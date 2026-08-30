import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { login as loginApi, register as registerApi, loginWithPhone } from "../api";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [mode, setMode] = useState("login"); // "login" | "register" | "phone-login"
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [inviteCode, setInviteCode] = useState("");
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (mode === "register") {
        if (!name.trim()) throw new Error("Name is required");
        const res = await registerApi(name, email, password, inviteCode || undefined);
        login(res.data.token, res.data.user);
        navigate("/dashboard", { state: { justRegistered: true } });
      } else if (mode === "phone-login") {
        const res = await loginWithPhone(phone, password);
        login(res.data.token, res.data.user);
        navigate("/dashboard");
      } else {
        const res = await loginApi(email, password);
        const { token, user, needsPassword } = res.data;
        login(token, user);
        if (needsPassword) {
          navigate("/set-password");
        } else {
          navigate("/dashboard");
        }
      }
    } catch (err) {
      setError(err.message || "An error occurred");
    } finally {
      setLoading(false);
    }
  };

  const titles = {
    "login": "Welcome Back",
    "register": "Create Account",
    "phone-login": "Karyakarta Login",
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
      {/* Background glow */}
      <div className="pointer-events-none fixed inset-x-0 top-0 h-64 bg-[radial-gradient(60%_100%_at_50%_0%,var(--color-primary-soft),transparent_70%)] opacity-25 dark:opacity-15" />

      <div className="relative w-full max-w-md surface-lift rounded-3xl p-8 border border-border">
        <div className="text-center mb-8">
          <img
            src="/logo.png"
            alt="Shree Samarth Ganesh Mandal"
            className="w-20 h-20 mx-auto mb-4 rounded-2xl object-contain"
          />
          <h1 className="font-display text-2xl font-semibold tracking-tight text-foreground">
            {titles[mode]}
          </h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Mandal Management System
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === "register" && (
            <>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">
                  Full Name
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Enter your name"
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">
                  Invite Code (Optional)
                </label>
                <input
                  type="text"
                  value={inviteCode}
                  onChange={(e) => setInviteCode(e.target.value)}
                  placeholder="e.g. MANDAL-ABC123"
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
                />
              </div>
            </>
          )}

          {mode === "phone-login" ? (
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
          ) : (
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">
                Email Address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
                required
              />
            </div>
          )}
          
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
              required
              minLength={6}
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
            {loading ? "..." : (mode === "register" ? "Create Account" : "Sign In")}
          </button>
        </form>
        
        <div className="mt-6 space-y-2 text-center">
          {mode === "login" && (
            <>
              <button 
                type="button"
                onClick={() => { setMode("phone-login"); setError(""); }}
                className="text-primary hover:text-primary/80 text-sm font-medium transition-colors block w-full"
              >
                Karyakarta? Login with Phone
              </button>
              <button 
                type="button"
                onClick={() => { setMode("register"); setError(""); }}
                className="text-muted-foreground hover:text-foreground text-sm transition-colors block w-full"
              >
                Don't have an account? Sign up
              </button>
            </>
          )}
          {mode === "register" && (
            <button 
              type="button"
              onClick={() => { setMode("login"); setError(""); }}
              className="text-primary hover:text-primary/80 text-sm font-medium transition-colors"
            >
              Already have an account? Sign in
            </button>
          )}
          {mode === "phone-login" && (
            <button 
              type="button"
              onClick={() => { setMode("login"); setError(""); }}
              className="text-primary hover:text-primary/80 text-sm font-medium transition-colors"
            >
              ← Back to Email Login
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
