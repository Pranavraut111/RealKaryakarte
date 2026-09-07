import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { memberJoin } from "../api";
import { Clock, CheckCircle2 } from "lucide-react";

export default function MemberJoinPage() {
  const navigate = useNavigate();
  const { inviteCode } = useParams();
  const { login } = useAuth();

  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await memberJoin(name, phone, inviteCode);
      const { token, user, needsPassword, pending: isPending } = res.data;

      if (isPending) {
        // Don't login — show pending screen
        setPending(true);
        return;
      }

      login(token, user);

      if (needsPassword) {
        navigate("/set-password");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(err.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  // ── Pending Approval Screen ──────────────────────────────────────────
  if (pending) {
    return (
      <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
        <div className="pointer-events-none fixed inset-x-0 top-0 h-64 bg-[radial-gradient(60%_100%_at_50%_0%,var(--color-primary-soft),transparent_70%)] opacity-25 dark:opacity-15" />

        <div className="relative w-full max-w-md surface-lift rounded-3xl p-8 border border-border text-center">
          <div className="w-20 h-20 mx-auto mb-6 rounded-full bg-amber-500/10 flex items-center justify-center">
            <Clock className="h-10 w-10 text-amber-500" />
          </div>

          <h1 className="font-display text-2xl font-semibold tracking-tight text-foreground mb-2">
            Approval Pending
          </h1>
          <p className="text-muted-foreground text-sm leading-relaxed mb-6">
            Your request to join the Mandal has been sent to the Admin.
            <br />
            You will get access once they approve your request.
          </p>

          <div className="rounded-2xl bg-muted/30 p-4 text-sm text-muted-foreground">
            <div className="flex items-center gap-2 justify-center">
              <CheckCircle2 className="h-4 w-4 text-green-500" />
              <span>Name: <strong className="text-foreground">{name}</strong></span>
            </div>
            <div className="flex items-center gap-2 justify-center mt-1">
              <CheckCircle2 className="h-4 w-4 text-green-500" />
              <span>Phone: <strong className="text-foreground">{phone}</strong></span>
            </div>
          </div>

          <p className="text-xs text-muted-foreground mt-6">
            Come back later and enter the same phone number to check your status.
          </p>
        </div>
      </div>
    );
  }

  // ── Join Form ────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
      {/* Background glow */}
      <div className="pointer-events-none fixed inset-x-0 top-0 h-64 bg-[radial-gradient(60%_100%_at_50%_0%,var(--color-primary-soft),transparent_70%)] opacity-25 dark:opacity-15" />

      <div className="relative w-full max-w-md surface-lift rounded-3xl p-8 border border-border">
        <div className="text-center mb-8">
          <img
            src="/logo.png"
            alt="Mandal"
            className="w-20 h-20 mx-auto mb-4 rounded-2xl object-contain"
          />
          <h1 className="font-display text-2xl font-semibold tracking-tight text-foreground">
            Join Mandal
          </h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Enter your name and phone number to continue
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">
              Your Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your full name"
              className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
              required
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">
              Phone Number
            </label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="Enter your phone number"
              className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
              required
            />
          </div>

          {error && (
            <div className="rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="accent-gradient mt-2 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-50"
          >
            {loading ? "Joining..." : "Enter →"}
          </button>
        </form>

        <div className="mt-6 text-center">
          <button
            type="button"
            onClick={() => navigate("/login")}
            className="text-primary hover:text-primary/80 text-sm font-medium transition-colors"
          >
            Admin / Karyakarta? Sign in here
          </button>
        </div>
      </div>
    </div>
  );
}
