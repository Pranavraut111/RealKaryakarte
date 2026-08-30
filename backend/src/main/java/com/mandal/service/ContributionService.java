package com.mandal.service;

import com.mandal.dao.ContributionDao;
import com.mandal.dao.SocietyRoomDao;
import com.mandal.dao.UserDao;
import com.mandal.dto.ContributionRequest;
import com.mandal.model.Contribution;
import com.mandal.model.PaymentMethod;
import com.mandal.model.SocietyRoom;
import com.mandal.model.User;
import com.mandal.util.ConfigUtil;
import com.mandal.util.ReceiptPdfGenerator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

/**
 * Business logic for contributions (vargani).
 * Enforces role-based access, 24h edit window for karyakartas,
 * auto-generates receipt numbers.
 */
public class ContributionService {

    private final ContributionDao contributionDao = new ContributionDao();
    private final SocietyRoomDao roomDao = new SocietyRoomDao();
    private final UserDao userDao = new UserDao();

    private final String receiptPrefix = ConfigUtil.get("mandal.receipt.prefix", "GM");

    /**
     * Add a new contribution.
     * Only ADMIN and KARYAKARTA can add contributions.
     */
    public Contribution addContribution(ContributionRequest req, Long userId, String userRole, Long mandalId)
            throws SQLException {

        // ── Role check ──────────────────────────────────────────────────
        if (!"ADMIN".equals(userRole) && !"KARYAKARTA".equals(userRole)) {
            throw new SecurityException("Only Admin or Karyakarta can add contributions");
        }

        // ── Validation ──────────────────────────────────────────────────
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (req.getContributionDate() == null) {
            throw new IllegalArgumentException("Contribution date is required");
        }

        // ── Build contribution ──────────────────────────────────────────
        Contribution c = new Contribution();
        if (req.getMemberId() != null && req.getMemberId() == 0) {
            req.setMemberId(null);
        }
        c.setMemberId(req.getMemberId());

        // Resolve member name
        if (req.getMemberId() != null) {
            User member = userDao.findById(req.getMemberId());
            c.setMemberName(member != null ? member.getName() : req.getMemberName());
        } else {
            c.setMemberName(req.getMemberName());
        }

        c.setAmount(req.getAmount());
        c.setPaymentMethod(PaymentMethod.fromString(
                req.getPaymentMethod() != null ? req.getPaymentMethod() : "CASH"));
        c.setCollectedBy(userId);
        if (req.getCollectedByName() != null && !req.getCollectedByName().isBlank()) {
            c.setCollectedByName(req.getCollectedByName());
        }
        c.setNote(req.getNote());
        c.setContributionDate(req.getContributionDate());
        c.setCreatedBy(userId);
        c.setMandalId(mandalId);
        c.setRoomNumber(req.getRoomNumber());
        c.setFloorNumber(req.getFloorNumber());
        c.setPhone(req.getPhone());

        // ── Generate receipt number ─────────────────────────────────────
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String receiptNo = contributionDao.nextReceiptNo(receiptPrefix, year);
        c.setReceiptNo(receiptNo);

        // ── Insert ──────────────────────────────────────────────────────
        Contribution saved = contributionDao.insert(c);

        // ── Generate PDF ──────────────────────────────
        if (saved != null) {
            String mName = "Ganpati Mandal";
            try {
                com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();
                com.mandal.model.Mandal m = mandalDao.findById(mandalId);
                if (m != null) mName = m.getMandalName();
            } catch (Exception e) {}
            
            String collectorName = "Karyakarta";
            if (saved.getCollectedByName() != null && !saved.getCollectedByName().isBlank()) {
                collectorName = saved.getCollectedByName();
            } else {
                User collector = userDao.findById(userId);
                if (collector != null) collectorName = collector.getName();
            }

            String pdfUrl = ReceiptPdfGenerator.generate(
                    saved.getReceiptNo(),
                    saved.getMemberName(),
                    saved.getAmount().toPlainString(),
                    saved.getPaymentMethod() != null ? saved.getPaymentMethod().name() : "",
                    saved.getContributionDate().toString(),
                    mName,
                    "en",
                    collectorName,
                    req.getRoomNumber(),
                    req.getFloorNumber()
            );
            if (pdfUrl != null) {
                saved.setReceiptPdfUrl(pdfUrl);
                contributionDao.update(saved);
            }
        }

        // ── Sync with Room Tracker ──────────────────────────────────────
        if (saved != null) {
            syncRoomFromContribution(req.getRoomNumber(), req.getFloorNumber(),
                    saved, userId, mandalId);
        }

        return saved;
    }

    /**
     * Update an existing contribution.
     * Karyakartas can edit their own entries within 24h.
     * Admins can edit anytime.
     */
    public Contribution updateContribution(Long contributionId, ContributionRequest req,
                                            Long userId, String userRole, Long mandalId) throws SQLException {
        Contribution existing = contributionDao.findById(contributionId, mandalId);
        if (existing == null) {
            throw new IllegalArgumentException("Contribution not found");
        }

        // ── Authorization ───────────────────────────────────────────────
        if ("KARYAKARTA".equals(userRole)) {
            // Must be the creator
            if (!userId.equals(existing.getCreatedBy())) {
                throw new SecurityException("You can only edit your own entries");
            }
            // Must be within 24h
            if (existing.getCreatedAt() != null &&
                    existing.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
                throw new SecurityException("Edit window (24h) has expired");
            }
        } else if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only Admin or Karyakarta can edit contributions");
        }

        // ── Update fields ───────────────────────────────────────────────
        if (req.getMemberId() != null) {
            existing.setMemberId(req.getMemberId() == 0 ? null : req.getMemberId());
        }
        if (req.getMemberName() != null) existing.setMemberName(req.getMemberName());
        if (req.getAmount() != null) existing.setAmount(req.getAmount());
        if (req.getPaymentMethod() != null) {
            existing.setPaymentMethod(PaymentMethod.fromString(req.getPaymentMethod()));
        }
        if (req.getNote() != null) existing.setNote(req.getNote());
        if (req.getContributionDate() != null) existing.setContributionDate(req.getContributionDate());
        if (req.getCollectedByName() != null) {
            existing.setCollectedByName(req.getCollectedByName().isBlank() ? null : req.getCollectedByName());
        }
        // Room and phone info (always overwrite — null clears it)
        existing.setRoomNumber(req.getRoomNumber());
        existing.setFloorNumber(req.getFloorNumber());
        existing.setPhone(req.getPhone());

        Contribution saved = contributionDao.update(existing);

        // ── Regenerate PDF ──────────────────────────────
        if (saved != null) {
            String mName = "Ganpati Mandal";
            try {
                com.mandal.dao.MandalDao mandalDao = new com.mandal.dao.MandalDao();
                com.mandal.model.Mandal m = mandalDao.findById(mandalId);
                if (m != null) mName = m.getMandalName();
            } catch (Exception e) {}
            
            String collectorName = "Karyakarta";
            if (saved.getCollectedByName() != null && !saved.getCollectedByName().isBlank()) {
                collectorName = saved.getCollectedByName();
            } else if (saved.getCollectedBy() != null) {
                User collector = userDao.findById(saved.getCollectedBy());
                if (collector != null) collectorName = collector.getName();
            }

            String pdfUrl = ReceiptPdfGenerator.generate(
                    saved.getReceiptNo(),
                    saved.getMemberName(),
                    saved.getAmount().toPlainString(),
                    saved.getPaymentMethod() != null ? saved.getPaymentMethod().name() : "",
                    saved.getContributionDate().toString(),
                    mName,
                    "en",
                    collectorName,
                    req.getRoomNumber(),
                    req.getFloorNumber()
            );
            if (pdfUrl != null) {
                saved.setReceiptPdfUrl(pdfUrl);
                contributionDao.update(saved); // save pdf url again just in case
            }
        }

        // ── Sync with Room Tracker ──────────────────────────────────────
        if (saved != null) {
            // First, clear any old room link this contribution had
            roomDao.clearContributionLink(saved.getId(), mandalId);
            // Then link to the new room (if room info is provided)
            syncRoomFromContribution(req.getRoomNumber(), req.getFloorNumber(),
                    saved, userId, mandalId);
        }

        return saved;
    }

    /**
     * Delete a contribution.
     * Same authorization rules as update.
     */
    public boolean deleteContribution(Long contributionId, Long userId, String userRole, Long mandalId)
            throws SQLException {
        Contribution existing = contributionDao.findById(contributionId, mandalId);
        if (existing == null) {
            throw new IllegalArgumentException("Contribution not found");
        }

        if ("KARYAKARTA".equals(userRole)) {
            if (!userId.equals(existing.getCreatedBy())) {
                throw new SecurityException("You can only delete your own entries");
            }
            if (existing.getCreatedAt() != null &&
                    existing.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
                throw new SecurityException("Delete window (24h) has expired");
            }
        } else if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Only Admin or Karyakarta can delete contributions");
        }

        // ── Revert room status before deleting ────────────────────────
        roomDao.clearContributionLink(contributionId, mandalId);

        return contributionDao.delete(contributionId, mandalId);
    }

    public Contribution getById(Long id, Long mandalId) throws SQLException {
        return contributionDao.findById(id, mandalId);
    }

    public List<Contribution> getAll(Long mandalId, LocalDate from, LocalDate to,
                                      Long memberId, String method) throws SQLException {
        return contributionDao.findAll(mandalId, from, to, memberId, method);
    }

    // ─── Room Tracker Sync ──────────────────────────────────────────────

    /**
     * Automatically marks a society_rooms entry as PAID when a contribution
     * includes room info. Silently skips if the room doesn't exist in the tracker.
     */
    private void syncRoomFromContribution(String roomNumber, Integer floorNumber,
                                           Contribution contribution, Long userId, Long mandalId) {
        try {
            if (roomNumber == null || roomNumber.isBlank()) return;
            int floor = (floorNumber != null && floorNumber >= 0) ? floorNumber : 1;

            SocietyRoom room = roomDao.findByRoomAndFloor(mandalId, roomNumber, floor);

            // Auto-create the room entry if it doesn't exist
            if (room == null) {
                room = new SocietyRoom();
                room.setMandalId(mandalId);
                room.setRoomNumber(roomNumber);
                room.setFloorNumber(floor);
                room.setResidentName(contribution.getMemberName());
                if (contribution.getPhone() != null && !contribution.getPhone().isBlank()) {
                    room.setResidentPhone(contribution.getPhone());
                }
                room.setVarganiStatus("PAID");
                room.setAmountPaid(contribution.getAmount());
                room = roomDao.insert(room);
                if (room == null) return;
            }

            room.setVarganiStatus("PAID");
            room.setAmountPaid(contribution.getAmount());
            room.setContributionId(contribution.getId());
            room.setMarkedBy(userId);
            room.setMarkedAt(LocalDateTime.now());

            // Update resident name from contribution if room has no name yet
            if ((room.getResidentName() == null || room.getResidentName().isBlank())
                    && contribution.getMemberName() != null) {
                room.setResidentName(contribution.getMemberName());
            }
            
            // Update phone if provided
            if (contribution.getPhone() != null && !contribution.getPhone().isBlank()) {
                room.setResidentPhone(contribution.getPhone());
            }

            roomDao.update(room);
        } catch (Exception e) {
            // Non-critical: don't fail the contribution if room sync fails
            System.err.println("[ContributionService] Room sync failed (non-critical): " + e.getMessage());
        }
    }
}
