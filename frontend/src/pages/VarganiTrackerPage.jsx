import { useEffect, useState } from "react";
import { Search, Plus, Check, Clock, AlertCircle, ChevronRight, X, Download, Building2, Trash2, Edit2, Home, Users } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

const statusConfig = {
  PAID:           { label: "paid",          bg: "bg-emerald-500/10", text: "text-emerald-600", dot: "bg-emerald-500" },
  PENDING:        { label: "pending",       bg: "bg-amber-500/10",   text: "text-amber-600",   dot: "bg-amber-500" },
  PARTIALLY_PAID: { label: "partiallyPaid", bg: "bg-blue-500/10",    text: "text-blue-600",    dot: "bg-blue-500" },
};

const FLOOR_LABELS = { 0: "talMajla", 1: "pahilaMajla", 2: "dusraMajla", 3: "tisraMajla" };
const floorLabel = (n, t) => FLOOR_LABELS[n] ? t(FLOOR_LABELS[n]) : `${t("floor")} ${n}`;

export default function VarganiTrackerPage() {
  const { user } = useAuth();
  const { t } = useLang();
  const [rooms, setRooms] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [activeTab, setActiveTab] = useState("OWNER");
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

  useEffect(() => { setLoading(true); loadData(); }, [filter, activeTab]);

  const filtered = rooms.filter(
    (r) =>
      r.roomNumber?.toLowerCase().includes(search.toLowerCase()) ||
      r.residentName?.toLowerCase().includes(search.toLowerCase())
  );

  // Summary
  const totalCollected = summary?.totalCollected ?? 0;
  const ownerCollected = summary?.ownerCollected ?? 0;
  const renterCollected = summary?.renterCollected ?? 0;
  const ownerTotal = summary?.ownerTotal ?? 0;
  const ownerPaid = summary?.ownerPaid ?? 0;
  const renterTotal = summary?.renterTotal ?? 0;
  const renterPaid = summary?.renterPaid ?? 0;
  const cashCollected = summary?.cashCollected ?? 0;
  const onlineCollected = summary?.onlineCollected ?? 0;
  const tabTotal = activeTab === "OWNER" ? ownerTotal : renterTotal;
  const tabPaid = activeTab === "OWNER" ? ownerPaid : renterPaid;
  const tabCollected = activeTab === "OWNER" ? ownerCollected : renterCollected;
  const progress = tabTotal > 0 ? Math.round((tabPaid / tabTotal) * 100) : 0;

  return (
    <AppShell title={t("varganiTracker")} subtitle={t("trackerSubtitle")}>
      {/* Tab Switcher */}
      <div className="mb-4 grid grid-cols-2 gap-1.5 rounded-2xl bg-secondary/50 p-1.5">
        {[
          { key: "OWNER", icon: Home, label: "gharmalak" },
          { key: "RENTER", icon: Users, label: "bhadekaru" },
        ].map(({ key, icon: Icon, label }) => (
          <button
            key={key}
            onClick={() => { setActiveTab(key); setFilter("ALL"); }}
            className={`flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold transition-all ${
              activeTab === key ? "bg-background text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Icon className="h-4 w-4" /> {t(label)}
          </button>
        ))}
      </div>

      {/* Summary Card */}
      <section className="ink-panel glow-accent relative overflow-hidden rounded-3xl p-5">
        <div className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-[var(--color-primary)] opacity-30 blur-3xl" />
        <div className="relative">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-[10px] font-medium uppercase tracking-[0.2em] text-ink-foreground/50">
                {activeTab === "OWNER" ? t("ekunGharmalak") : t("ekunBhadekaru")}
              </p>
              <p className="font-display tabular mt-1 text-3xl font-semibold text-ink-foreground">
                {tabPaid}/{tabTotal}
              </p>
              <p className="mt-0.5 text-xs text-ink-foreground/60">
                {activeTab === "OWNER" ? t("ownersPaid") : t("rentersPaid")}
              </p>
            </div>
            {/* Circular progress */}
            <div className="relative h-16 w-16">
              <svg className="h-16 w-16 -rotate-90" viewBox="0 0 80 80">
                <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor" className="text-ink-foreground/10" strokeWidth="6" />
                <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor" className="text-emerald-500" strokeWidth="6"
                  strokeLinecap="round" strokeDasharray={`${2 * Math.PI * 34}`}
                  strokeDashoffset={`${2 * Math.PI * 34 * (1 - progress / 100)}`}
                  style={{ transition: "stroke-dashoffset 0.5s ease" }} />
              </svg>
              <span className="absolute inset-0 flex items-center justify-center font-display text-sm font-bold text-ink-foreground">{progress}%</span>
            </div>
          </div>
          {/* Amount row */}
          <div className="mt-4 grid grid-cols-3 gap-2 text-center">
            <div className="rounded-lg bg-ink-foreground/6 px-2 py-1.5">
              <p className="text-[9px] uppercase tracking-wider text-ink-foreground/45">{t("rakkam")}</p>
              <p className="tabular text-sm font-semibold text-emerald-400">{inr(tabCollected)}</p>
            </div>
            <div className="rounded-lg bg-ink-foreground/6 px-2 py-1.5">
              <p className="text-[9px] uppercase tracking-wider text-ink-foreground/45">{t("cashJama")}</p>
              <p className="tabular text-sm font-semibold text-ink-foreground">{inr(cashCollected)}</p>
            </div>
            <div className="rounded-lg bg-ink-foreground/6 px-2 py-1.5">
              <p className="text-[9px] uppercase tracking-wider text-ink-foreground/45">{t("onlineJama")}</p>
              <p className="tabular text-sm font-semibold text-ink-foreground">{inr(onlineCollected)}</p>
            </div>
          </div>
          {/* Grand total */}
          <div className="mt-2 flex items-center justify-between rounded-lg bg-ink-foreground/6 px-3 py-1.5">
            <span className="text-[9px] uppercase tracking-wider text-ink-foreground/45">{t("ekunVarganiJama")}</span>
            <span className="tabular text-sm font-semibold text-emerald-400">{inr(totalCollected)}</span>
          </div>
        </div>
      </section>

      {/* Action buttons */}
      {canEdit && (
        <section className="mt-3 grid grid-cols-2 gap-2">
          <button onClick={() => setShowBulkAdd(true)}
            className="flex min-h-[48px] items-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-medium transition-transform active:scale-[0.98] accent-gradient text-primary-foreground shadow-[var(--shadow-float)]">
            <Building2 className="h-4 w-4" /> {t("bulkAdd")}
          </button>
          <button
            onClick={() => {
              const today = new Date().toISOString().split("T")[0];
              api.downloadReport("2020-01-01", today).catch((err) => alert("Export failed: " + err.message));
            }}
            className="flex min-h-[48px] items-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-medium transition-transform active:scale-[0.98] surface-lift text-foreground">
            <Download className="h-4 w-4" /> {t("exportExcel")}
          </button>
        </section>
      )}

      {/* Filters + Search */}
      <section className="mt-4">
        <div className="mb-3 flex gap-1.5 overflow-x-auto">
          {["ALL", "PAID", "PENDING", "PARTIALLY_PAID"].map((f) => (
            <button key={f} onClick={() => setFilter(f)}
              className={`whitespace-nowrap rounded-full px-3.5 py-1.5 text-xs font-semibold transition-colors ${
                filter === f ? "bg-primary text-primary-foreground" : "bg-secondary text-muted-foreground hover:text-foreground"
              }`}>
              {f === "ALL" ? t("all") : t(statusConfig[f]?.label || f)}
            </button>
          ))}
        </div>
        <div className="relative mb-3">
          <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input type="text" placeholder={`${t("roomNumber")} / ${t("residentName")}...`}
            value={search} onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-input bg-background/50 py-2.5 pl-10 pr-4 text-sm outline-none focus:border-primary" />
        </div>
      </section>

      {/* Room List */}
      <section className="pb-24">
        {loading ? (
          <div className="flex h-32 items-center justify-center">
            <div className="h-7 w-7 animate-spin rounded-full border-3 border-primary border-r-transparent" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="surface-lift rounded-2xl px-6 py-12 text-center">
            <Building2 className="mx-auto mb-2 h-8 w-8 text-muted-foreground" />
            <p className="font-display text-base font-semibold">{t("noRoomsYet")}</p>
            <p className="mt-1 text-xs text-muted-foreground">{t("setupRoomsDesc")}</p>
            {canEdit && (
              <button onClick={() => setShowBulkAdd(true)}
                className="accent-gradient mt-3 inline-flex items-center gap-1.5 rounded-xl px-5 py-2 text-sm font-semibold text-primary-foreground">
                <Plus className="h-4 w-4" /> {t("bulkAdd")}
              </button>
            )}
          </div>
        ) : activeTab === "OWNER" ? (
          /* ── OWNER: Simple table-like list ── */
          <div className="surface-lift overflow-hidden rounded-2xl">
            {/* Table header */}
            <div className="grid grid-cols-[48px_1fr_80px_60px] gap-1 border-b border-border/50 bg-secondary/40 px-3 py-2">
              <span className="text-[10px] font-semibold uppercase text-muted-foreground">{t("roomNumber")}</span>
              <span className="text-[10px] font-semibold uppercase text-muted-foreground">{t("nav")}</span>
              <span className="text-right text-[10px] font-semibold uppercase text-muted-foreground">{t("rakkam")}</span>
              <span className="text-right text-[10px] font-semibold uppercase text-muted-foreground">{t("sthiti")}</span>
            </div>
            {/* Rows */}
            <div className="divide-y divide-border/30">
              {filtered.map((room) => {
                const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;
                return (
                  <div key={room.id}
                    className="grid grid-cols-[48px_1fr_80px_60px] items-center gap-1 px-3 py-2.5 cursor-pointer hover:bg-secondary/20 transition-colors"
                    onClick={() => canEdit && setMarkRoom(room)}>
                    <span className="font-display text-sm font-bold text-primary">{room.roomNumber}</span>
                    <div className="min-w-0">
                      <p className="truncate text-sm text-foreground">{room.residentName || "—"}</p>
                      {room.paymentMethod && (
                        <span className="text-[10px] text-muted-foreground">
                          {room.paymentMethod === "CASH" ? t("cashJama") : t("onlineJama")}
                        </span>
                      )}
                    </div>
                    <span className="tabular text-right text-sm font-medium text-emerald-600">
                      {room.amountPaid > 0 ? inr(room.amountPaid) : "—"}
                    </span>
                    <span className={`ml-auto inline-flex rounded-full px-2 py-0.5 text-[9px] font-bold ${cfg.bg} ${cfg.text}`}>
                      {t(cfg.label)}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        ) : (
          /* ── RENTER: Grouped by floor, compact rows ── */
          <div className="space-y-3">
            {(() => {
              // Group by floor
              const byFloor = {};
              filtered.forEach((r) => {
                const key = r.floorNumber;
                if (!byFloor[key]) byFloor[key] = [];
                byFloor[key].push(r);
              });
              return Object.entries(byFloor)
                .sort(([a], [b]) => Number(a) - Number(b))
                .map(([floorNum, floorRooms]) => {
                  const paidInFloor = floorRooms.filter(r => r.varganiStatus === "PAID").length;
                  return (
                    <div key={floorNum} className="surface-lift overflow-hidden rounded-2xl">
                      {/* Floor header */}
                      <div className="flex items-center justify-between border-b border-border/50 bg-secondary/40 px-3 py-2">
                        <span className="text-xs font-bold text-foreground">
                          {floorLabel(Number(floorNum), t)}
                        </span>
                        <span className="text-[10px] font-semibold text-muted-foreground">{paidInFloor}/{floorRooms.length}</span>
                      </div>
                      {/* Table header */}
                      <div className="grid grid-cols-[40px_1fr_70px_55px] gap-1 border-b border-border/20 px-3 py-1.5">
                        <span className="text-[9px] font-semibold uppercase text-muted-foreground/60">#</span>
                        <span className="text-[9px] font-semibold uppercase text-muted-foreground/60">{t("nav")}</span>
                        <span className="text-right text-[9px] font-semibold uppercase text-muted-foreground/60">{t("rakkam")}</span>
                        <span className="text-right text-[9px] font-semibold uppercase text-muted-foreground/60">{t("sthiti")}</span>
                      </div>
                      {/* Compact rows */}
                      <div className="divide-y divide-border/20">
                        {floorRooms
                          .sort((a, b) => (parseInt(a.roomNumber)||999) - (parseInt(b.roomNumber)||999))
                          .map((room) => {
                            const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;
                            return (
                              <div key={room.id}
                                className="grid grid-cols-[40px_1fr_70px_55px] items-center gap-1 px-3 py-2 cursor-pointer hover:bg-secondary/20 transition-colors"
                                onClick={() => canEdit && setMarkRoom(room)}>
                                <span className="text-xs font-bold text-primary">{room.roomNumber}</span>
                                <div className="min-w-0">
                                  <p className="truncate text-xs text-foreground">{room.residentName || "—"}</p>
                                </div>
                                <span className="tabular text-right text-xs font-medium text-emerald-600">
                                  {room.amountPaid > 0 ? inr(room.amountPaid) : "—"}
                                </span>
                                <span className={`ml-auto flex h-5 items-center rounded-full px-1.5 text-[8px] font-bold ${cfg.bg} ${cfg.text}`}>
                                  {t(cfg.label)}
                                </span>
                              </div>
                            );
                          })}
                      </div>
                    </div>
                  );
                });
            })()}
          </div>
        )}
      </section>

      {/* FAB */}
      {canEdit && (
        <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
          <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
            <button onClick={() => setShowAddRoom(true)}
              className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
              aria-label="Add Room">
              <Plus className="h-6 w-6" />
            </button>
          </div>
        </div>
      )}

      {showBulkAdd && <BulkAddSheet onClose={() => setShowBulkAdd(false)} onSuccess={() => { setShowBulkAdd(false); loadData(); }} />}
      {(showAddRoom || editRoom) && (
        <AddRoomSheet initialData={editRoom} activeTab={activeTab}
          onClose={() => { setShowAddRoom(false); setEditRoom(null); }}
          onSuccess={() => { setShowAddRoom(false); setEditRoom(null); loadData(); }} />
      )}
      {markRoom && (
        <MarkStatusSheet room={markRoom}
          onClose={() => setMarkRoom(null)}
          onSuccess={() => { setMarkRoom(null); loadData(); }}
          onEdit={() => { setEditRoom(markRoom); setMarkRoom(null); }}
          onDelete={() => { setMarkRoom(null); loadData(); }} />
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
    setError(""); setLoading(true);
    try {
      const res = await api.bulkAddRooms({
        roomStart: Number(form.roomStart), roomEnd: Number(form.roomEnd),
        floorStart: Number(form.floorStart), floorEnd: Number(form.floorEnd),
        includeOwner: form.includeOwner,
      });
      setResult(res.data?.count || res.message);
      setTimeout(onSuccess, 1200);
    } catch (err) { setError(err.message || "Failed"); } finally { setLoading(false); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div className="w-full max-w-xl rounded-t-3xl bg-background p-6 pb-28 max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-lg font-semibold">{t("bulkAdd")}</h3>
          <button onClick={onClose} className="rounded-full p-2 hover:bg-secondary"><X className="h-5 w-5" /></button>
        </div>
        {result ? (
          <div className="py-8 text-center">
            <Check className="mx-auto mb-3 h-10 w-10 text-emerald-500" />
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
    e.preventDefault(); setError(""); setLoading(true);
    try {
      if (initialData) await api.updateRoom(initialData.id, form);
      else await api.addRoom(form);
      onSuccess();
    } catch (err) { setError(err.message || "Failed"); } finally { setLoading(false); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div className="w-full max-w-xl rounded-t-3xl bg-background p-6 pb-28 max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}>
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
    e.preventDefault(); setLoading(true);
    try {
      await api.markRoomStatus(room.id, { varganiStatus: status, amountPaid: Number(amount) || 0, notes });
      onSuccess();
    } catch (err) { alert(err.message); } finally { setLoading(false); }
  };

  const handleDelete = async () => {
    if (!confirm("Delete this room entry?")) return;
    setDeleting(true);
    try { await api.deleteRoom(room.id); onDelete(); }
    catch (err) { alert(err.message); } finally { setDeleting(false); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div className="w-full max-w-xl rounded-t-3xl bg-background p-6 pb-28 max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom"
        onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-display text-base font-semibold">
              {t("roomNumber")} {room.roomNumber}
              {room.residentType === "OWNER" ? ` · ${t("gharmalak")}` : ` · ${floorLabel(room.floorNumber, t)}`}
            </h3>
            <p className="text-xs text-muted-foreground">
              {room.residentName || "—"}{room.residentPhone && ` · ${room.residentPhone}`}
            </p>
          </div>
          <button onClick={onClose} className="rounded-full p-2 hover:bg-secondary"><X className="h-5 w-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="grid grid-cols-3 gap-2">
            {["PAID", "PENDING", "PARTIALLY_PAID"].map((s) => {
              const sc = statusConfig[s];
              return (
                <button key={s} type="button"
                  onClick={() => { setStatus(s); if (s === "PENDING") setAmount("0"); }}
                  className={`rounded-xl border-2 px-2 py-2 text-center text-xs font-semibold transition-colors ${
                    status === s ? `border-current ${sc.text} ${sc.bg}` : "border-transparent bg-secondary text-muted-foreground"
                  }`}>
                  {t(sc.label)}
                </button>
              );
            })}
          </div>
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("amountPaid")} (₹)</label>
            <input type="number" value={amount} onChange={(e) => setAmount(e.target.value)}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-2.5 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-muted-foreground">{t("addNotes")}</label>
            <input type="text" value={notes} onChange={(e) => setNotes(e.target.value)}
              className="mt-1 w-full rounded-xl border border-input bg-background px-4 py-2.5 text-sm" />
          </div>
          <button type="submit" disabled={loading}
            className="accent-gradient flex w-full items-center justify-center gap-2 rounded-2xl py-3 text-sm font-semibold text-primary-foreground disabled:opacity-50">
            {loading ? t("saving") : t("updateStatus")}
          </button>
          <div className="flex gap-2">
            <button type="button" onClick={onEdit}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-input py-2.5 text-xs font-medium text-foreground hover:bg-secondary">
              <Edit2 className="h-3.5 w-3.5" /> {t("editRoom")}
            </button>
            <button type="button" onClick={handleDelete} disabled={deleting}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-red-500/30 py-2.5 text-xs font-medium text-red-500 hover:bg-red-500/10 disabled:opacity-50">
              <Trash2 className="h-3.5 w-3.5" /> {t("delete")}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
