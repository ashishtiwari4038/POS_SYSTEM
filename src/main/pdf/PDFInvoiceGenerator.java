package main.pdf;

import main.models.Sale;

public class PDFInvoiceGenerator {
    
    public static String generateInvoice(Sale sale, String cashierName) {
        
        return InvoiceGenerator.generateInvoice(sale, cashierName);
    }
}