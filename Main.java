import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// --- 1. Product Hierarchy (Inheritance) ---

/**
 * Abstract base class representing a generic product.
 */
abstract class Product {
    private String id;
    private String name;
    private double price;

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    // Abstract method to be implemented by subclasses
    public abstract String getDisplayDetails();
}

/**
 * Represents physical items that require shipping.
 */
class PhysicalProduct extends Product {
    private double weightKg;
    private double shippingCost;

    public PhysicalProduct(String id, String name, double price, double weightKg, double shippingCost) {
        super(id, name, price);
        this.weightKg = weightKg;
        this.shippingCost = shippingCost;
    }

    public double getShippingCost() { return shippingCost; }

    @Override
    public String getDisplayDetails() {
        return String.format("%s [Physical] - $%.2f (+ $%.2f shipping) - Weight: %.1fkg", 
            getName(), getPrice(), shippingCost, weightKg);
    }
}

/**
 * Represents digital items (ebooks, software) with no shipping cost.
 */
class DigitalProduct extends Product {
    private String downloadLink;

    public DigitalProduct(String id, String name, double price) {
        super(id, name, price);
        this.downloadLink = "http://store.com/download/" + id;
    }

    @Override
    public String getDisplayDetails() {
        return String.format("%s [Digital] - $%.2f - Instant Download", getName(), getPrice());
    }
}

// --- 2. Cart Objects (Composition) ---

/**
 * Represents a specific line item in the cart (Product + Quantity).
 */
class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

/**
 * Manages the list of items and calculations.
 */
class ShoppingCart {
    private List<CartItem> items;
    private double discountPercentage; // 0.0 to 1.0 (e.g., 0.10 is 10% off)

    public ShoppingCart() {
        this.items = new ArrayList<>();
        this.discountPercentage = 0.0;
    }

    public void addProduct(Product product, int quantity) {
        // Check if item already exists, if so, update quantity
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                // In a real app, we'd update the existing item. 
                // For simplicity here, we'll just add a new entry or you could merge them.
                // Let's just add it as a new line item for this demo.
            }
        }
        items.add(new CartItem(product, quantity));
        System.out.println("Added " + quantity + " x " + product.getName() + " to cart.");
    }

    public void removeProduct(int index) {
        if (index >= 0 && index < items.size()) {
            CartItem removed = items.remove(index);
            System.out.println("Removed " + removed.getProduct().getName() + " from cart.");
        } else {
            System.out.println("Invalid item selection.");
        }
    }

    public void applyCoupon(String code) {
        // Simple Hardcoded Discount Logic
        if (code.equalsIgnoreCase("JAVA20")) {
            this.discountPercentage = 0.20;
            System.out.println("Success! 20% discount applied.");
        } else if (code.equalsIgnoreCase("WELCOME10")) {
            this.discountPercentage = 0.10;
            System.out.println("Success! 10% discount applied.");
        } else {
            System.out.println("Invalid or expired coupon code.");
            this.discountPercentage = 0.0;
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void viewCart() {
        if (items.isEmpty()) {
            System.out.println("\n--- Your Cart is Empty ---");
            return;
        }

        System.out.println("\n--- Current Cart ---");
        double subtotal = 0;
        double totalShipping = 0;

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            Product p = item.getProduct();
            
            double itemTotal = item.getTotalPrice();
            subtotal += itemTotal;

            // Handle shipping logic based on class type
            double shipping = 0;
            if (p instanceof PhysicalProduct) {
                shipping = ((PhysicalProduct) p).getShippingCost() * item.getQuantity();
                totalShipping += shipping;
            }

            System.out.printf("%d. %-20s | Qty: %d | Price: $%.2f | Sub: $%.2f %s%n", 
                (i + 1), 
                p.getName(), 
                item.getQuantity(), 
                p.getPrice(), 
                itemTotal,
                (shipping > 0 ? "(+ $" + String.format("%.2f", shipping) + " ship)" : "")
            );
        }

        double discountAmount = subtotal * discountPercentage;
        double finalTotal = (subtotal - discountAmount) + totalShipping;

        System.out.println("------------------------------------------------");
        System.out.printf("Subtotal:          $%.2f%n", subtotal);
        if (discountPercentage > 0) {
            System.out.printf("Discount (%.0f%%):   -$%.2f%n", (discountPercentage * 100), discountAmount);
        }
        System.out.printf("Shipping:          $%.2f%n", totalShipping);
        System.out.printf("TOTAL:             $%.2f%n", finalTotal);
        System.out.println("------------------------------------------------");
    }
    
    public double getFinalTotal() {
        double subtotal = 0;
        double shipping = 0;
        for(CartItem item : items) {
            subtotal += item.getTotalPrice();
            if(item.getProduct() instanceof PhysicalProduct) {
                shipping += ((PhysicalProduct) item.getProduct()).getShippingCost() * item.getQuantity();
            }
        }
        return (subtotal - (subtotal * discountPercentage)) + shipping;
    }
    
    public void clear() {
        items.clear();
        discountPercentage = 0.0;
    }
}

// --- 3. Main Application ---

public class SimpleECommerce {
    
    private static List<Product> inventory = new ArrayList<>();
    private static ShoppingCart cart = new ShoppingCart();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeInventory();
        
        System.out.println("Welcome to the Java Console Store!");
        
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayInventory();
                    break;
                case "2":
                    addToCartFlow();
                    break;
                case "3":
                    cart.viewCart();
                    break;
                case "4":
                    removeFromCartFlow();
                    break;
                case "5":
                    applyDiscountFlow();
                    break;
                case "6":
                    checkoutFlow();
                    break;
                case "7":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void initializeInventory() {
        // Polymorphism in action: List<Product> holding different subclasses
        inventory.add(new PhysicalProduct("P001", "Mechanical Keyboard", 89.99, 1.2, 10.00));
        inventory.add(new PhysicalProduct("P002", "Gaming Mouse", 45.50, 0.3, 5.00));
        inventory.add(new PhysicalProduct("P003", "27in Monitor", 199.99, 5.5, 25.00));
        inventory.add(new DigitalProduct("D001", "Java Masterclass Ebook", 29.99));
        inventory.add(new DigitalProduct("D002", "Antivirus Software", 19.99));
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View Inventory");
        System.out.println("2. Add Item to Cart");
        System.out.println("3. View Cart");
        System.out.println("4. Remove Item from Cart");
        System.out.println("5. Apply Discount Code");
        System.out.println("6. Checkout");
        System.out.println("7. Exit");
    }

    private static void displayInventory() {
        System.out.println("\n--- Store Inventory ---");
        for (int i = 0; i < inventory.size(); i++) {
            System.out.printf("%d. %s%n", (i + 1), inventory.get(i).getDisplayDetails());
        }
    }

    private static void addToCartFlow() {
        displayInventory();
        System.out.print("Enter product number to add: ");
        try {
            int productIdx = Integer.parseInt(scanner.nextLine()) - 1;
            if (productIdx >= 0 && productIdx < inventory.size()) {
                System.out.print("Enter quantity: ");
                int qty = Integer.parseInt(scanner.nextLine());
                if (qty > 0) {
                    cart.addProduct(inventory.get(productIdx), qty);
                } else {
                    System.out.println("Quantity must be positive.");
                }
            } else {
                System.out.println("Invalid product number.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numbers only.");
        }
    }

    private static void removeFromCartFlow() {
        cart.viewCart();
        if (cart.isEmpty()) return;

        System.out.print("Enter line number to remove: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            cart.removeProduct(idx);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void applyDiscountFlow() {
        System.out.print("Enter coupon code (Try 'JAVA20' or 'WELCOME10'): ");
        String code = scanner.nextLine().trim();
        cart.applyCoupon(code);
    }

    private static void checkoutFlow() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty. Add items first.");
            return;
        }
        
        cart.viewCart();
        System.out.printf("Final Total to charge: $%.2f%n", cart.getFinalTotal());
        System.out.print("Confirm purchase? (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            System.out.println("Processing payment...");
            System.out.println("Payment Successful! Thank you for your order.");
            cart.clear();
        } else {
            System.out.println("Checkout cancelled.");
        }
    }
}
