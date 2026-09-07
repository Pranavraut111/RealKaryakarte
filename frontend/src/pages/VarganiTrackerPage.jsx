import { useEffect, useState } from "react";
import { Search, Plus, Check, Clock, AlertCircle, ChevronRight, X, Download, Building2, Trash2, Edit2, Home, Users } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

const statusConfig = {
  PAID:           { label: "paid",          bg: "bg-emerald-500/10", text: "text-emerald-600", icon: Check, dot: "bg-emerald-500" },
  PENDING:        { label: "pending",       bg: "bg-amber-500/10",   text: "text-amber-600",   icon: Clock, dot: "bg-amber-500" },
  PARTIALLY_PAID: { label: "partiallyPaid", bg: "bg-blue-500/10",    text: "text-blue-600",    icon: AlertCircle, dot: "bg-blue-500" },
};

const FLOOR_LABELS = {
  0: "talMajla",
  1: "pahilaMajla",
  2: "dusraMajla",
  3: "tisraMajla",
};

const floorLabel = (floorNumber, t) => {
  if (FLOOR_LABELS[floorNumber]) return t(FLOOR_LABELS[floorNumber]);
  return `${t("floor")} ${floorNumber}`;
};

export default function VarganiTrackerPage() {
  const { user } = useAuth();
  const { t } = useLang();
  const [rooms, setRooms] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [activeTab, setActiveTab] = useState("OWNER"); // OWNER or RENTER
  const [showBulkAdd, setShowBulkAdd] = useState(false);
  const [showAddRoom, setShowAddRoom] = useState(false);
  const [editRoom, setEditRoom] = useState(null);
  const [markRoom, setMarkRoom] = useState(null);

  const canEdit = user?.role === "ADMIN" || user?.role === "KARYAKARTA";

  const loadData = async () => {
    try {
      const params = {};
      if (filter !== "ALL") params.status = filter;
      params.type = activeTab;
      const [roomsRes, summaryRes] = await Promise.all([
        api.getRooms(params),
        api.getRoomsSummary(),
      ]);
      setRooms(roomsRes.data || roomsRes || []);
      setSummary(summaryRes.data || summaryRes || {});
    } catch (err) {
      console.error("Failed to load tracker data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    loadData();
  }, [filter, activeTab]);

  const filtered = rooms.filter(
    (r) =>
      r.roomNumber?.toLowerCase().includes(search.toLowerCase()) ||
      r.residentName?.toLowerCase().includes(search.toLowerCase())
  );

  // ── Grouping logic ──────────────────────────────────────────────────
  // Owners: group by room number (flat list, no floor subgroups)
  // Renters: group by floor first, then by room number
  const grouped = {};
  if (activeTab === "OWNER") {
    // Simple list grouped by room number
    filtered.forEach((r) => {
      const key = r.roomNumber;
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(r);
    });
  } else {
    // Group by floor
    filtered.forEach((r) => {
      const floorKey = r.floorNumber;
      if (!grouped[floorKey]) grouped[floorKey] = [];
      grouped[floorKey].push(r);
    });
  }

  // Summary values
  const totalCollected = summary?.totalCollected ?? 0;
  const ownerTotal = summary?.ownerTotal ?? 0;
  const ownerPaid = summary?.ownerPaid ?? 0;
  const ownerCollected = summary?.ownerCollected ?? 0;
  const renterTotal = summary?.renterTotal ?? 0;
  const renterPaid = summary?.renterPaid ?? 0;
  const renterCollected = summary?.renterCollected ?? 0;
  const cashCollected = summary?.cashCollected ?? 0;
  const onlineCollected = summary?.onlineCollected ?? 0;

  const tabTotal = activeTab === "OWNER" ? ownerTotal : renterTotal;
  const tabPaid = activeTab === "OWNER" ? ownerPaid : renterPaid;
  const tabCollected = activeTab === "OWNER" ? ownerCollected : renterCollected;
  const progress = tabTotal > 0 ? Math.round((tabPaid / tabTotal) * 100) : 0;

  return (
    <AppShell title={t("varganiTracker")} subtitle={t("trackerSubtitle")}>

      {/* ── Tab Switcher: घरमालक / भाडेकरू ─────────────────────────── */}
      <div className="mb-4 grid grid-cols-2 gap-2 rounded-2xl bg-secondary/50 p-1.5">
        <button
          onClick={() => { setActiveTab("OWNER"); setFilter("ALL"); }}
          className={`flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold transition-all ${
            activeTab === "OWNER"
              ? "bg-background text-foreground shadow-sm"
              : "text-muted-foreground hover:text-foreground"
          }`}
        >
          <Home className="h-4 w-4" />
          {t("gharmalak")}
        </button>
        <button
          onClick={() => { setActiveTab("RENTER"); setFilter("ALL"); }}
          className={`flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold transition-all ${
            activeTab === "RENTER"
              ? "bg-background text-foreground shadow-sm"
              : "text-muted-foreground hover:text-foreground"
          }`}
        >
          <Users className="h-4 w-4" />
          {t("bhadekaru")}
        </button>
      </div>

      {/* ── Summary Card ────────────────────────────────────────────── */}
      <section className="ink-panel glow-accent relative overflow-hidden rounded-3xl p-6">
        <div className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-[var(--color-primary)] opacity-30 blur-3xl" />
        <div className="relative">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium uppercase tracking-[0.24em] text-ink-foreground/60">
                {activeTab === "OWNER" ? t("ekunGharmalak") : t("ekunBhadekaru")}
              </p>
              <p className="font-display tabular mt-2 text-4xl font-semibold text-ink-foreground">
                {tabPaid}/{tabTotal}
              </p>
              <p className="mt-1 text-sm text-ink-foreground/70">
                {activeTab === "OWNER" ? t("ownersPaid") : t("rentersPaid")}
              </p>
            </div>
            <div className="flex flex-col items-center">
              {/* Circular progress */}
              <div className="relative h-20 w-20">
                <svg className="h-20 w-20 -rotate-90" viewBox="0 0 80 80">
                  <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor"
                    className="text-ink-foreground/10" strokeWidth="6" />
                  <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor"
                    className="text-emerald-500" strokeWidth="6"
                    strokeLinecap="round"
                    strokeDasharray={`${2 * Math.PI * 34}`}
                    strokeDashoffset={`${2 * Math.PI * 34 * (1 - progress / 100)}`}
                    style={{ transition: "stroke-dashoffset 0.5s ease" }}
                  />
                </svg>
                <span className="absolute inset-0 flex items-center justify-center font-display text-lg font-bold text-ink-foreground">
                  {progress}%
                </span>
              </div>
            </div>
          </div>

          {/* Collected amount + cash/online breakdown */}
          <div className="mt-5 grid grid-cols-3 gap-2">
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("rakkam")}</p>
              <p className="tabular mt-0.5 text-base font-semibold text-emerald-400">{inr(tabCollected)}</p>
            </div>
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("cashJama")}</p>
              <p className="tabular mt-0.5 text-base font-semibold text-ink-foreground">{inr(cashCollected)}</p>
            </div>
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("onlineJama")}</p>
              <p className="tabular mt-0.5 text-base font-semibold text-ink-foreground">{inr(onlineCollected)}</p>
            </div>
          </div>

          {/* Grand total bar */}
          <div className="mt-3 rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-4 py-2.5">
            <div className="flex justify-between items-center">
              <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("ekunVarganiJama")}</p>
              <p className="tabular text-lg font-semibold text-emerald-400">{inr(totalCollected)}</p>
            </div>
            <div className="flex justify-between items-center mt-0.5">
              <p className="text-[10px] text-ink-foreground/40">{t("ekunGharmalak")}: {inr(ownerCollected)}</p>
              <p className="text-[10px] text-ink-foreground/40">{t("ekunBhadekaru")}: {inr(renterCollected)}</p>
            </div>
          </div>
        </div>
      </section>

      {/* Action buttons */}
      {canEdit && (
        <section className="mt-4 grid grid-cols-2 gap-3">
          <button
            onClick={() => setShowBulkAdd(true)}
            className="flex min-h-[56px] items-center gap-2.5 whitespace-nowrap rounded-2xl px-4 py-3 text-sm font-medium transition-transform active:scale-[0.98] accent-gradient text-primary-foreground shadow-[var(--shadow-float)]"
          >
            <Building2 className="h-4 w-4" />
            {t("bulkAdd")}
            <Plus className="ml-auto h-4 w-4 opacity-70" />
          </button>
          <button
            onClick={() => {
              const today = new Date().toISOString().split("T")[0];
              api.downloadReport("2020-01-01", today).catch((err) => alert("Export failed: " + err.message));
            }}
            className="flex min-h-[56px] items-center gap-2.5 whitespace-nowrap rounded-2xl px-4 py-3 text-sm font-medium transition-transform active:scale-[0.98] surface-lift text-foreground"
          >
            <Download className="h-4 w-4" />
            {t("exportExcel")}
          </button>
        </section>
      )}

      {/* Filters + Search */}
      <section className="mt-6">
        <div className="mb-4 flex gap-2">
          {["ALL", "PAID", "PENDING", "PARTIALLY_PAID"].map((f) => {
            const active = filter === f;
            const lbl = f === "ALL" ? t("all") : t(statusConfig[f]?.label || f);
            return (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
                  active
                    ? "bg-primary text-primary-foreground"
                    : "bg-secondary text-muted-foreground hover:text-foreground"
                }`}
              >
                {lbl}
              </button>
            );
          })}
        </div>

        <div className="relative mb-4">
          <Search className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            placeholder={`${t("roomNumber")} / ${t("residentName")}...`}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-2xl border border-input bg-background/50 py-3.5 pl-12 pr-4 text-sm outline-none transition-colors focus:border-primary"
          />
        </div>
      </section>

      {/* ── Room List ───────────────────────────────────────────────── */}
      <section className="pb-24">
        {loading ? (
          <div className="flex h-40 items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-r-transparent" />
          </div>
        ) : Object.keys(grouped).length === 0 ? (
          <div className="surface-lift rounded-2xl px-6 py-14 text-center">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-secondary text-muted-foreground">
              <Building2 className="h-6 w-6" />
            </div>
            <p className="font-display text-lg font-semibold">{t("noRoomsYet")}</p>
            <p className="mt-1 text-sm text-muted-foreground">{t("setupRoomsDesc")}</p>
            {canEdit && (
              <button
                onClick={() => setShowBulkAdd(true)}
                className="accent-gradient mt-4 inline-flex min-h-[44px] items-center gap-2 rounded-2xl px-6 text-sm font-semibold text-primary-foreground"
              >
                <Plus className="h-4 w-4" /> {t("bulkAdd")}
              </button>
            )}
          </div>
        ) : activeTab === "OWNER" ? (
          /* ── Owner View: Simple flat list by room number ──────── */
          <div className="space-y-2">
            {Object.entries(grouped).map(([roomNum, roomEntries]) => (
              <div key={roomNum} className="surface-lift overflow-hidden rounded-2xl">
                {roomEntries.map((room) => {
                  const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;
                  const StatusIcon = cfg.icon;
                  return (
                    <div
                      key={room.id}
                      className="flex items-center gap-3 px-4 py-3.5 cursor-pointer hover:bg-secondary/20 transition-colors"
                      onClick={() => canEdit && setMarkRoom(room)}
                    >
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 font-display text-sm font-bold text-primary">
                        {roomNum}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-foreground">
                          {room.residentName || <span className="text-muted-foreground italic">{t("nav")}</span>}
                        </p>
                        <p className="text-[11px] text-muted-foreground">
                          {t("roomNumber")} {roomNum}
                          {room.residentPhone && ` · ${room.residentPhone}`}
                          {room.paymentMethod && ` · ${room.paymentMethod === "CASH" ? t("cashJama") : t("onlineJama")}`}
                        </p>
                      </div>
                      <div className="text-right flex items-center gap-2">
                        {room.amountPaid > 0 && (
                          <span className="tabular text-sm font-semibold text-emerald-600">
                            {inr(room.amountPaid)}
                          </span>
                        )}
                        <span className={`inline-flex rounded-full px-2.5 py-0.5 text-[10px] font-semibold ${cfg.bg} ${cfg.text}`}>
                          {t(cfg.label)}
                        </span>
                        {canEdit && <ChevronRight className="h-4 w-4 text-muted-foreground/40" />}
                      </div>
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        ) : (
          /* ── Renter View: Grouped by floor ────────────────────── */
          <div className="space-y-4">
            {Object.entries(grouped)
              .sort(([a], [b]) => Number(a) - Number(b))
              .map(([floorNum, floorRooms]) => {
                const paidInFloor = floorRooms.filter(r => r.varganiStatus === "PAID").length;
                return (
                  <div key={floorNum} className="surface-lift overflow-hidden rounded-2xl">
                    {/* Floor header */}
                    <div className="flex items-center gap-3 border-b border-border/50 bg-secondary/30 px-4 py-3">
                      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                        <Building2 className="h-4 w-4 text-primary" />
                      </div>
                      <p className="text-sm font-semibold text-foreground">
                        {floorLabel(Number(floorNum), t)}
                      </p>
                      <p className="ml-auto text-xs text-muted-foreground">
                        {paidInFloor}/{floorRooms.length}
                      </p>
                    </div>
                    {/* Room entries under this floor */}
                    <div className="divide-y divide-border/50">
                      {floorRooms
                        .sort((a, b) => {
                          const na = parseInt(a.roomNumber) || 999999;
                          const nb = parseInt(b.roomNumber) || 999999;
                          return na - nb;
                        })
                        .map((room) => {
                          const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;
                          const StatusIcon = cfg.icon;
                          return (
                            <div
                              key={room.id}
                              className="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-secondary/20 transition-colors"
                              onClick={() => canEdit && setMarkRoom(room)}
                            >
                              <div className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${cfg.bg}`}>
                                <StatusIcon className={`h-4 w-4 ${cfg.text}`} />
                              </div>
                              <div className="min-w-0 flex-1">
                                <p className="truncate text-sm font-medium text-foreground">
                                  {room.residentName || <span className="text-muted-foreground italic">{t("nav")}</span>}
                                </p>
                                <p className="text-[11px] text-muted-foreground">
                                  {t("roomNumber")} {room.roomNumber}
                                  {room.residentPhone && ` · ${room.residentPhone}`}
                                  {room.paymentMethod && ` · ${room.paymentMethod === "CASH" ? t("cashJama") : t("onlineJama")}`}
                                </p>
                              </div>
                              <div className="text-right flex items-center gap-2">
                                {room.amountPaid > 0 && (
                                  <span className="tabular text-sm font-semibold text-emerald-600">
                                    {inr(room.amountPaid)}
                                  </span>
                                )}
                                <span className={`inline-flex rounded-full px-2.5 py-0.5 text-[10px] font-semibold ${cfg.bg} ${cfg.text}`}>
                                  {t(cfg.label)}
                                </span>
                                {canEdit && <ChevronRight className="h-4 w-4 text-muted-foreground/40" />}
                              </div>
                            </div>
                          );
                        })}
                    </div>
                  </div>
                );
              })}
          </div>
        )}
      </section>

      {/* FAB for adding a single room */}
      {canEdit && (
        <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
          <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
            <button
              onClick={() => setShowAddRoom(true)}
              className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
              aria-label="Add Room"
            >
              <Plus className="h-6 w-6" />
            </button>
          </div>
        </div>
      )}

      {/* Bulk Add Sheet */}
      {showBulkAdd && (
        <BulkAddSheet
          onClose={() => setShowBulkAdd(false)}
          onSuccess={() => { setShowBulkAdd(false); loadData(); }}
        />
      )}

      {/* Add/Edit Room Sheet */}
      {(showAddRoom || editRoom) && (
        <AddRoomSheet
          initialData={editRoom}
          activeTab={activeTab}
          onClose={() => { setShowAddRoom(false); setEditRoom(null); }}
          onSuccess={() => { setShowAddRoom(false); setEditRoom(null); loadData(); }}
        />
      )}

      {/* Mark Status Sheet */}
      {markRoom && (
        <MarkStatusSheet
          room={markRoom}
          onClose={() => setMarkRoom(null)}
          onSuccess={() => { setMarkRoom(null); loadData(); }}
          onEdit={() => { setEditRoom(markRoom); setMarkRoom(null); }}
          onDelete={() => { setMarkRoom(null); loadData(); }}
        />
      )}
    </AppShell>
  );
}

// ─── Bulk Add Sheet ─────────────────────────────────────────────────────────

function BulkAddSheet({ onClose, onSuccess }) {
  const { t } = useLang();
  const [form, setForm] = useState({ roomStart: "", roomEnd: "", floorStart: "1", floorEnd: "3", includeOwner: true });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await api.bulkAddRooms({
        roomStart: Number(form.roomStart),
        roomEnd: Number(form.roomEnd),
        floorStart: Number(form.floorStart),
        floorEnd: Number(form.floorEnd),
        includeOwner: form.includeOwner,
      });
      setResult(res.data?.count || res.message);
      setTimeout(onSuccess, 1200);
    } catch (err) {
      setError(err.message || "Failed to create rooms");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-xl rounded-t-3xl bg-background p-6 animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-lg font-semibold">{t("bulkAdd")}</h3>
          <button onClick={onClose} className="rounded-full p-2 hover:bg-secondary"><X className="h-5 w-5" /></button>
        </div>

        {result ? (
          <div className="py-8 text-center">
            <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-emerald-500/10">
              <Check className="h-7 w-7 text-emerald-500" />
            </div>
            <p className="font-display text-lg font-semibold">{result} rooms created!</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-muted-foreground">{t("roomFrom")}</label>
                <input type="number" value={form.roomStart} onChange={(e) => setForm({ ...form, roomStart: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
              </div>
              <div>
                <label className="text-xs font-medium text-muted-foreground">{t("roomTo")}</label>
                <input type="number" value={form.roomEnd} onChange={(e) => setForm({ ...form, roomEnd: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-muted-foreground">{t("floorFrom")}</label>
                <input type="number" value={form.floorStart} onChange={(e) => setForm({ ...form, floorStart: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
              </div>
              <div>
                <label className="text-xs font-medium text-muted-foreground">{t("floorTo")}</label>
                <input type="number" value={form.floorEnd} onChange={(e) => setForm({ ...form, floorEnd: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
              </div>
            </div>

            {/* Include Owner checkbox */}
            <label className="flex items-start gap-3 rounded-xl border border-input p-3 cursor-pointer">
              <input type="checkbox" checked={form.includeOwner} onChange={(e) => setForm({ ...form, includeOwner: e.target.checked })}
                className="mt-0.5 h-5 w-5 rounded accent-[var(--color-primary)]" />
              <div>
                <p className="text-sm font-medium">{t("includeOwner")}</p>
                <p className="text-xs text-muted-foreground">{t("includeOwnerDesc")}</p>
              </div>
            </label>

            {error && <p className="text-sm text-red-500">{error}</p>}

            <button type="submit" disabled={loading}
              className="accent-gradient flex w-full items-center justify-center gap-2 rounded-2xl py-3.5 text-sm font-semibold text-primary-foreground disabled:opacity-50">
              {loading ? t("creating") : t("createRooms")}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}

// ─── Add / Edit Room Sheet ──────────────────────────────────────────────────

function AddRoomSheet({ initialData, activeTab, onClose, onSuccess }) {
  const { t } = useLang();
  const [form, setForm] = useState({
    roomNumber: initialData?.roomNumber || "",
    floorNumber: initialData?.floorNumber ?? (activeTab === "OWNER" ? 0 : 1),
    residentName: initialData?.residentName || "",
    residentPhone: initialData?.residentPhone || "",
    notes: initialData?.notes || "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      if (initialData) {
        await api.updateRoom(initialData.id, form);
      } else {
        await api.addRoom(form);
      }
      onSuccess();
    } catch (err) {
      setError(err.message || "Failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-xl rounded-t-3xl bg-background p-6 animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-lg font-semibold">{initialData ? t("editRoom") : t("addRoom")}</h3>
          <button onClick={onClose} className="rounded-full p-2 hover:bg-secondary"><X className="h-5 w-5" /></button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-medium text-muted-foreground">{t("roomNumber")}</label>
              <input type="text" value={form.roomNumber} onChange={(e) => setForm({ ...form, roomNumber: e.target.value })}
                className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
            </div>
            <div>
              <label className="text-xs font-medium text-muted-foreground">{t("floor")}</label>
              <input type="number" value={form.floorNumber} onChange={(e) => setForm({ ...form, floorNumber: Number(e.target.value) })}
                className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" required />
            </div>
          </div>
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("residentName")}</label>
            <input type="text" value={form.residentName} onChange={(e) => setForm({ ...form, residentName: e.target.value })}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("residentPhone")}</label>
            <input type="tel" value={form.residentPhone} onChange={(e) => setForm({ ...form, residentPhone: e.target.value })}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("addNotes")}</label>
            <input type="text" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" />
          </div>

          {error && <p className="text-sm text-red-500">{error}</p>}

          <button type="submit" disabled={loading}
            className="accent-gradient flex w-full items-center justify-center gap-2 rounded-2xl py-3.5 text-sm font-semibold text-primary-foreground disabled:opacity-50">
            {loading ? t("saving") : t("saveChanges")}
          </button>
        </form>
      </div>
    </div>
  );
}

// ─── Mark Status Sheet ──────────────────────────────────────────────────────

function MarkStatusSheet({ room, onClose, onSuccess, onEdit, onDelete }) {
  const { t } = useLang();
  const [status, setStatus] = useState(room.varganiStatus);
  const [amount, setAmount] = useState(room.amountPaid || "");
  const [notes, setNotes] = useState(room.notes || "");
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.markRoomStatus(room.id, {
        varganiStatus: status,
        amountPaid: Number(amount) || 0,
        notes,
      });
      onSuccess();
    } catch (err) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm("Delete this room entry?")) return;
    setDeleting(true);
    try {
      await api.deleteRoom(room.id);
      onDelete();
    } catch (err) {
      alert(err.message);
    } finally {
      setDeleting(false);
    }
  };

  const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-xl rounded-t-3xl bg-background p-6 animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <div>
            <h3 className="font-display text-lg font-semibold">
              {t("roomNumber")} {room.roomNumber}
              {room.residentType === "OWNER"
                ? ` · ${t("gharmalak")}`
                : ` · ${floorLabel(room.floorNumber, t)}`}
            </h3>
            <p className="text-sm text-muted-foreground">
              {room.residentName || "—"}
              {room.residentPhone && ` · ${room.residentPhone}`}
            </p>
          </div>
          <button onClick={onClose} className="rounded-full p-2 hover:bg-secondary"><X className="h-5 w-5" /></button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Status selector */}
          <div className="grid grid-cols-3 gap-2">
            {["PAID", "PENDING", "PARTIALLY_PAID"].map((s) => {
              const sc = statusConfig[s];
              const active = status === s;
              return (
                <button
                  key={s}
                  type="button"
                  onClick={() => {
                    setStatus(s);
                    if (s === "PENDING") setAmount("0");
                  }}
                  className={`rounded-xl border-2 px-3 py-2.5 text-center text-xs font-semibold transition-colors ${
                    active
                      ? `border-current ${sc.text} ${sc.bg}`
                      : "border-transparent bg-secondary text-muted-foreground"
                  }`}
                >
                  {t(sc.label)}
                </button>
              );
            })}
          </div>

          {/* Amount */}
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("amountPaid")} (₹)</label>
            <input type="number" value={amount} onChange={(e) => setAmount(e.target.value)}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" />
          </div>

          {/* Notes */}
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("addNotes")}</label>
            <input type="text" value={notes} onChange={(e) => setNotes(e.target.value)}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm" />
          </div>

          <button type="submit" disabled={loading}
            className="accent-gradient flex w-full items-center justify-center gap-2 rounded-2xl py-3.5 text-sm font-semibold text-primary-foreground disabled:opacity-50">
            {loading ? t("saving") : t("updateStatus")}
          </button>

          {/* Edit / Delete row */}
          <div className="flex gap-3">
            <button type="button" onClick={onEdit}
              className="flex flex-1 items-center justify-center gap-2 rounded-2xl border border-input py-3 text-sm font-medium text-foreground hover:bg-secondary">
              <Edit2 className="h-4 w-4" /> {t("editRoom")}
            </button>
            <button type="button" onClick={handleDelete} disabled={deleting}
              className="flex flex-1 items-center justify-center gap-2 rounded-2xl border border-red-500/30 py-3 text-sm font-medium text-red-500 hover:bg-red-500/10 disabled:opacity-50">
              <Trash2 className="h-4 w-4" /> {t("delete")}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
