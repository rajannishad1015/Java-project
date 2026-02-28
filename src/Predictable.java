// Interface for panels that can predict something
// Teacher told us to use interfaces for defining a "contract"
// If any class implements this interface, it must provide a predict() method

public interface Predictable {

    // Any panel that does prediction must implement this
    void predict();

    // default method - interface mein bhi body likh sakte hain (Java 8+)
    // ye sirf ek helper hai validation ke liye
    default boolean isValidNumber(String s) {
        try {
            double val = Double.parseDouble(s);
            return val > 0;
        } catch (NumberFormatException e) {
            // agar number nahi hai toh false return karo
            return false;
        }
    }
}
