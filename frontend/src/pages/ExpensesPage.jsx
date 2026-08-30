import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { Camera, Plus, X, Edit2 } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";

import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

export default function ExpensesPage() {
  const { user, mandalName } = useAuth();
  const { t } = useLang();
  const location = useLocation();
  const [expenses, setExpenses] = useState([]);
  const [showAddForm, setShowAddForm] = useState(location.state?.openForm || false);
  const [editItem, setEditItem] = useState(null);
  const [loading, setLoading] = useState(true);

  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    if (location.state?.openForm) {
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  const loadExpenses = () => {
    api.getExpenses()
      .then((res) => setExpenses(res.data || res || []))
      .catch((err) => console.error("Failed to load expenses:", err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadExpenses();
  }, []);

  const total = expenses.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);
  const canEdit = user?.role === "ADMIN" || user?.role === "KARYAKARTA";

  return (
    <AppShell title={t("expenses")}>
      <div className="flex h-full flex-col p-4 pt-6">
        <div className="mb-6 rounded-3xl bg-black p-6 text-white shadow-xl">
          <p className="text-sm font-medium text-white/70 uppercase tracking-widest">{mandalName || t("mandalName")}</p>
          <h1 className="mt-2 font-display text-4xl font-bold tracking-tight">{t("expenses")}</h1>
          <p className="mt-2 font-medium text-white/80">{expenses.length} {t("items")} · {inr(total)} {t("shown")}</p>
        </div>

        <div className="flex-1 overflow-y-auto pb-24">
          {loading ? (
            <div className="flex h-40 items-center justify-center">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-r-transparent" />
            </div>
          ) : (
            <div className="space-y-3">
              {expenses.map((e) => (
                <div 
                  key={e.id} 
                  className="surface-lift flex flex-col rounded-3xl p-4 cursor-pointer transition-colors hover:bg-secondary"
                  onClick={() => setExpandedId(expandedId === e.id ? null : e.id)}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-secondary/50 font-display font-semibold text-primary">
                        {e.itemName?.charAt(0) || "E"}
                      </div>
                      <div>
                        <p className="font-semibold text-foreground">{e.itemName || e.item}</p>
                        <p className="text-xs text-muted-foreground">{e.vendorName || e.vendor} · {e.expenseDate || e.date}</p>
                      </div>
                    </div>
                    <div className="text-right flex flex-col items-end gap-1">
                      <div className="flex items-center gap-2">
                        <p className="font-display font-semibold text-destructive">-{inr(e.amount)}</p>
                        {canEdit && (
                          <button 
                            onClick={(ev) => { ev.stopPropagation(); setEditItem(e); }} 
                            className="text-muted-foreground hover:text-primary transition-colors"
                          >
                            <Edit2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                      {e.receiptPhotoUrl && expandedId !== e.id && (
                        <div className="flex items-center gap-1 text-[10px] font-medium text-primary">
                          <Camera className="h-3 w-3" /> Has Photo
                        </div>
                      )}
                    </div>
                  </div>

                  {expandedId === e.id && (
                    <div className="mt-4 border-t border-border pt-4 animate-in slide-in-from-top-2">
                      <div className="grid grid-cols-2 gap-4 text-sm mb-4">
                        <div>
                          <p className="text-muted-foreground text-xs uppercase tracking-wider mb-0.5">Vendor</p>
                          <p className="font-medium text-foreground">{e.vendorName || "-"}</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground text-xs uppercase tracking-wider mb-0.5">Payment Method</p>
                          <p className="font-medium text-foreground">{e.paymentMethod || "CASH"}</p>
                        </div>
                        <div className="col-span-2">
                          <p className="text-muted-foreground text-xs uppercase tracking-wider mb-0.5">Purchased By</p>
                          <p className="font-medium text-foreground">{e.purchasedByName || "-"}</p>
                        </div>
                      </div>
                      
                      {e.receiptPhotoUrl && (
                        <div className="mt-2">
                          <p className="text-muted-foreground text-xs uppercase tracking-wider mb-2">Receipt / Item Photo</p>
                          <img 
                            src={(import.meta.env.VITE_API_BASE || "") + e.receiptPhotoUrl} 
                            alt="Receipt" 
                            className="w-full rounded-2xl border border-white/10 object-contain max-h-[350px] bg-foreground/5" 
                          />
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ))}
              
              {expenses.length === 0 && (
                <div className="mt-12 text-center text-muted-foreground">
                  <p>No expenses logged yet.</p>
                </div>
              )}
            </div>
          )}
        </div>

        {canEdit && (
          <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
            <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
              <button
                onClick={() => setShowAddForm(true)}
                className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
                aria-label="Add Expense"
              >
                <Plus className="h-6 w-6" />
              </button>
            </div>
          </div>
        )}
      </div>

      {(showAddForm || editItem) && (
        <AddExpenseSheet
          initialData={editItem}
          onClose={() => { setShowAddForm(false); setEditItem(null); }}
          onSuccess={() => {
            setShowAddForm(false);
            setEditItem(null);
            loadExpenses();
          }}
        />
      )}
    </AppShell>
  );
}

function AddExpenseSheet({ onClose, onSuccess, initialData }) {
  const { user } = useAuth();
  const { t } = useLang();
  const isEdit = !!initialData;
  const [formData, setFormData] = useState({
    itemName: initialData?.itemName || "",
    vendor: initialData?.vendorName || initialData?.vendor || "",
    purchasedByName: initialData?.purchasedByName || "",
    amount: initialData?.amount || "",
    paymentMethod: initialData?.paymentMethod || "CASH",
    expenseDate: initialData?.expenseDate || initialData?.date || new Date().toISOString().split("T")[0]
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      let photoUrl = initialData?.receiptPhotoUrl || null;
      if (file) {
        const uploadRes = await api.uploadFile(file);
        photoUrl = uploadRes.data;
      }

      const payload = {
        ...formData,
        amount: Number(formData.amount),
        vendorName: formData.vendor,
        purchasedByName: formData.purchasedByName,
        receiptPhotoUrl: photoUrl,
        submittedBy: user?.id
      };

      if (isEdit) {
        await api.updateExpense(initialData.id, payload);
      } else {
        await api.addExpense(payload);
      }
      
      onSuccess();
    } catch (err) {
      setError(err.message || "Failed to log expense");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-end justify-center">
      <button
        aria-label="Close form"
        onClick={onClose}
        className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in"
      />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300 h-[85vh] overflow-y-auto">
        <div className="glass rounded-t-3xl px-5 pb-8 pt-4 min-h-full">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-muted-foreground/40" />
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold">{isEdit ? t("editExpense") : t("newExpense")}</h2>
            <button
              onClick={onClose}
              className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary"
              aria-label="Close"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("itemName")}</label>
              <input
                required
                type="text"
                value={formData.itemName}
                onChange={e => setFormData({...formData, itemName: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("amount")} (₹)</label>
                <input
                  required
                  type="number"
                  min="1"
                  value={formData.amount}
                  onChange={e => setFormData({...formData, amount: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("vendor")}</label>
                <input
                  type="text"
                  placeholder="E.g. Shree Arts"
                  value={formData.vendor}
                  onChange={e => setFormData({...formData, vendor: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("purchasedBy")}</label>
              <input
                type="text"
                placeholder="E.g. Pranav"
                value={formData.purchasedByName}
                onChange={e => setFormData({...formData, purchasedByName: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("method")}</label>
                <select
                  value={formData.paymentMethod}
                  onChange={e => setFormData({...formData, paymentMethod: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                >
                  <option value="CASH">{t("cash")}</option>
                  <option value="UPI">{t("upi")}</option>
                  <option value="BANK_TRANSFER">{t("bankTransfer")}</option>
                  <option value="CHEQUE">{t("cheque")}</option>
                </select>
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("date")}</label>
                <input
                  required
                  type="date"
                  value={formData.expenseDate}
                  onChange={e => setFormData({...formData, expenseDate: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground flex items-center gap-2">
                <Camera className="h-4 w-4" /> {t("billPhoto")}
              </label>
              <input
                type="file"
                accept="image/*"
                onChange={e => setFile(e.target.files[0])}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-2.5 text-sm outline-none focus:border-primary file:mr-4 file:rounded-full file:border-0 file:bg-primary/10 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-primary hover:file:bg-primary/20 cursor-pointer"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="accent-gradient mt-4 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-70"
            >
              {loading ? t("saving") : isEdit ? t("saveExpense") : t("logExpense")}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
