package library0;

import java.util.Objects;

/**
 * Fine entity: represents a fine associated with a loan.
 */
public class Fine {

    private final String id;       // e.g., "F1"
    private final String userId;
    private final String loanId;
    private final int totalAmount; // e.g., 10 NIS
    private int paidAmount;        // how much has been paid

    public Fine(String id, String userId, String loanId, int totalAmount, int paidAmount) {
        if (totalAmount < 0 || paidAmount < 0) {
            throw new IllegalArgumentException("Amounts must be >= 0");
        }
        if (paidAmount > totalAmount) {
            throw new IllegalArgumentException("Paid cannot exceed total");
        }
        this.id = id;
        this.userId = userId;
        this.loanId = loanId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getLoanId() {
        return loanId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public int getPaidAmount() {
        return paidAmount;
    }

    public int getOutstandingAmount() {
        return totalAmount - paidAmount;
    }

    /**
     * Apply a payment toward this fine.
     * @return how much was actually applied.
     */
    public int applyPayment(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment must be > 0");
        }
        int outstanding = getOutstandingAmount();
        int applied = Math.min(outstanding, amount);
        this.paidAmount += applied;
        return applied;
    }

    public boolean isFullyPaid() {
        return getOutstandingAmount() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fine)) return false;
        Fine fine = (Fine) o;
        return Objects.equals(id, fine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}