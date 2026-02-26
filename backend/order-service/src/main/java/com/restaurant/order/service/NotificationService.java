package com.restaurant.order.service;

import com.restaurant.order.dto.MenuItemDto;
import com.restaurant.order.dto.MenuItemResponseDto;
import com.restaurant.order.entity.EmailLog;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;
import com.restaurant.order.repository.EmailLogRepository;
import com.restaurant.order.repository.OrderItemRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;

    @Value("${menu.service.url}")
    private String menuServiceUrl;

    @Value("${admin.email}")
    private String adminEmail;

    public NotificationService(JavaMailSender mailSender,
                               EmailLogRepository emailLogRepository,
                               OrderItemRepository orderItemRepository,
                               RestTemplate restTemplate) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
        this.orderItemRepository = orderItemRepository;
        this.restTemplate = restTemplate;
    }

    // REMOVED @Async - runs in same thread/transaction as caller
    public void sendOrderConfirmation(Order order) {
        sendEmail(order.getUserEmail(), "Your meal is being prepared! - #" + order.getId(),
                buildVillageOrderConfirmationHtml(order),order.getId());

        // Admin notification
        sendEmail(adminEmail, "New Order Received - #" + order.getId(),
                "New order #" + order.getId() + " placed by user " + order.getUserId() +
                        ". Total: ₹" + order.getFinalAmount() + ". Check dashboard.",order.getId());
    }

    // REMOVED @Async
    public void sendOrderUpdateEmail(Order order) {
        sendEmail(order.getUserEmail(), "Order Status Update - #" + order.getId(),
                buildOrderUpdateHtml(order),order.getId());
    }

    private void sendEmail(String to, String subject, String htmlContent, Long orderId) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("abhishekh.yb.dev@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("HTML EMAIL SUCCESS → To: " + to + " | Subject: " + subject);
            logEmail(to, subject, "SENT",orderId);
        } catch (Exception e) {
            System.err.println("HTML EMAIL FAILED → To: " + to);
            e.printStackTrace();
            logEmail(to, subject, "FAILED",orderId);
        }
    }

    private void logEmail(String to, String subject, String status,Long orderId) {
        EmailLog log = new EmailLog();
        log.setToEmail(to);
        log.setSubject(subject);
        log.setStatus(status);
        log.setOrderId(orderId);
        emailLogRepository.save(log);
    }

    private String getItemName(Long menuItemId) {
        String url = menuServiceUrl + "/" + menuItemId;
        System.out.println("[ITEM NAME FETCH] Calling URL: " + url);

        try {
            MenuItemResponseDto response = restTemplate.getForObject(url, MenuItemResponseDto.class);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                System.out.println("[ITEM NAME FETCH] Invalid response or no data");
                return "Item #" + menuItemId;
            }

            String name = response.getData().getName();
            if (name == null || name.trim().isEmpty()) {
                System.out.println("[ITEM NAME FETCH] Name is null or empty");
                return "Item #" + menuItemId;
            }

            System.out.println("[ITEM NAME FETCH] SUCCESS - Name: " + name);
            return name;
        } catch (Exception e) {
            System.err.println("[ITEM NAME FETCH] Error for ID " + menuItemId + ": " + e.getMessage());
            e.printStackTrace();
            return "Item #" + menuItemId;
        }
    }

    private int calculateTotalPrepTime(List<OrderItem> items) {
        return items.stream()
                .mapToInt(item -> {
                    String url = menuServiceUrl + "/" + item.getMenuItemId();
                    try {
                        MenuItemResponseDto response = restTemplate.getForObject(url, MenuItemResponseDto.class);
                        if (response != null && response.isSuccess() && response.getData() != null) {
                            int prep = response.getData().getPreparationTimeMinutes();
                            return prep > 0 ? prep : 20;
                        }
                    } catch (Exception e) {
                        System.err.println("Prep time fetch failed for item " + item.getMenuItemId());
                    }
                    return 20;
                })
                .max()
                .orElse(20);
    }

    private List<OrderItem> getOrderItems(Long orderId) {
        List<OrderItem> items = orderItemRepository.getOrderItemsByOrderId(orderId);
        System.out.println("[FINAL DEBUG] Order ID: " + orderId + " | Found " + items.size() + " items");
        if (items.isEmpty()) {
            System.out.println("[FINAL DEBUG] Query returned empty - should not happen after flush()");
        }
        return items;
    }

    private String buildVillageOrderConfirmationHtml(Order order) {
        StringBuilder itemsHtml = new StringBuilder();
        List<OrderItem> items = getOrderItems(order.getId());
        System.out.println("[EMAIL TEMPLATE] Order ID: " + order.getId() + " | Items count: " + items.size());

        for (OrderItem item : items) {
            String itemName = getItemName(item.getMenuItemId());
            itemsHtml.append(String.format(
                    "<tr>" +
                            "<td>%s</td>" +
                            "<td style=\"text-align:center\">%d</td>" +
                            "<td style=\"text-align:right\">₹%.2f</td>" +
                            "</tr>",
                    itemName,
                    item.getQuantity(),
                    item.getPrice()
            ));
        }

        int prepMinutes = calculateTotalPrepTime(items);
        int deliveryMinutes = order.getEstimatedDeliveryMinutes();
        int totalMinutes = prepMinutes + deliveryMinutes;

        LocalDateTime expectedTime = LocalDateTime.now().plusMinutes(totalMinutes);
        String expectedTimeStr = expectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"));

        String deliveryPartnerName = "Parthul S";
        String deliveryPartnerPhone = "+91 98765 43010";

        return """
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    background-color: #1a1a1a;
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    color: #ffffff;
                }
                .email-container {
                    max-width: 500px;
                    margin: 20px auto;
                    background-color: #121212;
                    border-radius: 15px;
                    overflow: hidden;
                    border: 1px solid #333;
                }
                .header {
                    background: linear-gradient(135deg, #ff8a65, #e64a19);
                    padding: 30px 20px;
                    text-align: center;
                }
                .header .emoji {
                    font-size: 50px;
                    margin-bottom: 10px;
                }
                .header h1 {
                    margin: 0;
                    font-size: 24px;
                    color: #fff;
                }
                .header p {
                    margin: 5px 0 0;
                    opacity: 0.9;
                    font-weight: bold;
                }
                .content {
                    padding: 25px;
                }
                .greeting {
                    font-size: 18px;
                    margin-bottom: 15px;
                }
                .message {
                    font-size: 15px;
                    line-height: 1.5;
                    margin-bottom: 20px;
                }
                .order-details {
                    background: #fff;
                    color: #333;
                    border-radius: 10px;
                    padding: 15px;
                    margin-top: 20px;
                }
                table {
                    width: 100%%;
                    border-collapse: collapse;
                }
                th {
                    text-align: left;
                    border-bottom: 1px solid #ddd;
                    padding-bottom: 8px;
                    font-size: 14px;
                }
                td {
                    padding: 10px 0;
                    font-size: 14px;
                }
                .total-row {
                    border-top: 2px solid #333;
                    font-weight: bold;
                    font-size: 18px;
                    padding-top: 15px;
                    display: flex;
                    justify-content: space-between;
                }
                .delivery-info {
                    margin-top: 25px;
                    font-size: 14px;
                    line-height: 1.6;
                    color: #ccc;
                }
                .delivery-info strong {
                    color: #ff8a65;
                }
                .pro-tip {
                    background: rgba(255, 138, 101, 0.1);
                    border-left: 4px solid #ff8a65;
                    padding: 12px;
                    font-style: italic;
                    margin: 20px 0;
                    font-size: 13px;
                }
                .btn-container {
                    text-align: center;
                    padding: 20px 0;
                }
                .track-btn {
                    background-color: #a3422e;
                    color: white;
                    padding: 15px 40px;
                    text-decoration: none;
                    border-radius: 30px;
                    font-weight: bold;
                    display: inline-block;
                }
                .footer {
                    text-align: center;
                    padding: 20px;
                    font-size: 13px;
                    color: #888;
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <div class="emoji">🏺</div>
                    <h1>Your meal is being prepared in our clay oven!</h1>
                    <p>Village Kitchen</p>
                </div>
                <div class="content">
                    <p class="greeting">Hello,</p>
                    <p class="message">
                        Your cravings are officially being handled. Our chefs have received your order and are starting to prep your meal right now. ❤️
                    </p>

                    <div class="order-details">
                        <p><strong>Order Details: <span style="color:#ff8a65;">#%d</span></strong></p>
                        <table>
                            <tr>
                                <th>Item</th>
                                <th>Qty</th>
                                <th>Price</th>
                            </tr>
                            %s
                            <tr class="total-row">
                                <td>Total Amount</td>
                                <td style="text-align:right">₹%.2f</td>
                            </tr>
                        </table>
                    </div>

                    <div class="delivery-info">
                        <p><strong>Delivering to:</strong> %s</p>
                        <p><strong>Estimated Arrival:</strong> Today, %s (Approx. %d mins)</p>
                        <p><strong>Delivery Partner:</strong> %s (%s)</p>
                    </div>

                    <div class="pro-tip">
                        "Pro Tip: Keep your phone nearby! Our rider will call you if they have trouble finding your gate."
                    </div>

                    <div class="btn-container">
                        <a href="#" class="track-btn">Track Your Order</a>
                    </div>
                </div>
                <div class="footer">
                    <p>Village Kitchen • Hyderabad, Telangana</p>
                    <p>Thank you for ordering with us!</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                order.getId(),
                itemsHtml.toString(),
                order.getFinalAmount(),
                order.getDeliveryAddress(),
                expectedTimeStr,
                totalMinutes,
                deliveryPartnerName,
                deliveryPartnerPhone
        );
    }

    private String buildOrderUpdateHtml(Order order) {
        return """
        <html>
        <head>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background: #f5f5f5; }
                .container { max-width: 500px; margin: 20px auto; background: #ffffff; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                .header { background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 30px; text-align: center; }
                .header h1 { margin: 0; font-size: 28px; }
                .content { padding: 30px; text-align: center; }
                .status { font-size: 32px; font-weight: bold; color: #4CAF50; margin: 20px 0; }
                .footer { background: #f8f8f8; padding: 25px; text-align: center; font-size: 14px; color: #777; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Order Update</h1>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p class="status">%s</p>
                    <p>Your order <strong>#%d</strong> has been updated to:</p>
                    <p style="font-size: 24px; font-weight: bold; color: #4CAF50;">%s</p>
                    <p>Estimated delivery time: <strong>%s</strong></p>
                    <p>Track your order anytime.</p>
                </div>
                <div class="footer">
                    <p>Village Kitchen • Hyderabad, Telangana</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                order.getStatus(),
                order.getId(),
                order.getStatus(),
                order.getExpectedDeliveryTime().toString()
        );
    }
}