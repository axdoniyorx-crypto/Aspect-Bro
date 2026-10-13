import React, { useState, useEffect } from 'react';
import {
  Shield,
  ShieldAlert,
  ShieldCheck,
  Search,
  AlertTriangle,
  FileCode,
  Smartphone,
  Trash2,
  Play,
  RotateCcw,
  Plus,
  CheckCircle2,
  XCircle,
  Copy,
  Flame,
  Radio,
  Wifi,
  Battery,
  Signal,
  Settings,
  X
} from 'lucide-react';

interface ApkItem {
  id: string;
  name: string;
  packageName: string;
  version: string;
  size: string;
  sha256: string;
  threatScore: number;
  severity: 'SAFE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  reasons: string[];
  dangerousPermissions: string[];
  isInstalled: boolean;
  isSystem: boolean;
}

interface MalwareSignature {
  hash: string;
  name: string;
  category: string;
  description: string;
}

const INITIAL_MALWARE_DB: MalwareSignature[] = [
  {
    hash: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    name: "Trojan.AndroidOS.Cerberus",
    category: "Banking Trojan",
    description: "Bank parollari va SMS tasdiqlash kodlarini o'g'irlovchi xavfli troyan"
  },
  {
    hash: "8f434346648f6b96df89dda901c5176b10f607629f7660cc6342d6f369456ac4",
    name: "Trojan-Banker.AndroidOS.SharkBot",
    category: "Banking Malware",
    description: "Avtomatik pul o'tkazish (ATS) va ekranni boshqarish virusi"
  },
  {
    hash: "4a5a54cf8e30b8042ebfbe1e7dd10d9f826315579ca2e4c9213190df0b5b5c92",
    name: "Spyware.AndroidOS.FluBot",
    category: "SMS Stealer & Spyware",
    description: "Soxta kuryerlik SMS tarqatib, butun kontaktlar va kartalarni o'g'irlovchi zararli dastur"
  },
  {
    hash: "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7",
    name: "Trojan.AndroidOS.Anatsa",
    category: "Keylogger & Remote Access",
    description: "Klaviatura bosishlarini yozib oluvchi va bank akkauntlarini buzuvchi troyan"
  },
  {
    hash: "c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2",
    name: "Ransomware.AndroidOS.Locker",
    category: "Ransomware (Tovlamachi)",
    description: "Telefon ekranini to'sib olib to'lov talab qiluvchi shantajchi virus"
  }
];

const INITIAL_APKS: ApkItem[] = [
  {
    id: "apk-1",
    name: "Bank_Update_Cerberus_Demo.apk",
    packageName: "com.trojan.banker.cerberus.fake",
    version: "2.1.0",
    size: "4.8 MB",
    sha256: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    threatScore: 98,
    severity: "CRITICAL",
    reasons: [
      "Qora ro'yxatdagi xavfli virus: Trojan.AndroidOS.Cerberus",
      "SMS xabarlar va OTP kodlarni o'g'irlash huquqi so'ralgan",
      "Bank ilovalari ustiga soxta kirish oynasi (Overlay Phishing) qo'yish ruxsati",
      "Maxsus imkoniyatlar (Accessibility) orqali tugmalarni bosish huquqi"
    ],
    dangerousPermissions: [
      "android.permission.RECEIVE_SMS",
      "android.permission.READ_SMS",
      "android.permission.SYSTEM_ALERT_WINDOW",
      "android.permission.BIND_ACCESSIBILITY_SERVICE"
    ],
    isInstalled: false,
    isSystem: false
  },
  {
    id: "apk-2",
    name: "Telegram_Premium_Crack.apk",
    packageName: "org.telegram.plus.mod.stealer",
    version: "10.4.1",
    size: "28.4 MB",
    sha256: "3d7a8c9b1f2e3d4c5b6a7e8f90123456789abcdef0123456789abcdef0123456",
    threatScore: 85,
    severity: "HIGH",
    reasons: [
      "Soxta klon ilova: Rasmiy Telegram emas, ichida zararli troyan kodi bor",
      "Kontaktlar va telefon qo'ng'iroqlari tarixini yashirin o'qish",
      "Mikrofon orqali yashirin ovoz yozib olish (Spyware)"
    ],
    dangerousPermissions: [
      "android.permission.RECORD_AUDIO",
      "android.permission.READ_CONTACTS",
      "android.permission.READ_CALL_LOG",
      "android.permission.REQUEST_INSTALL_PACKAGES"
    ],
    isInstalled: false,
    isSystem: false
  },
  {
    id: "apk-3",
    name: "System_Update_Fake.apk",
    packageName: "com.android.system.service.update",
    version: "1.0",
    size: "1.5 MB",
    sha256: "4a5a54cf8e30b8042ebfbe1e7dd10d9f826315579ca2e4c9213190df0b5b5c92",
    threatScore: 92,
    severity: "CRITICAL",
    reasons: [
      "Soxta tizim yangilanishi niqobi ostidagi FluBot troyani",
      "Qurilma administratori (Device Admin) huquqini so'rash",
      "Avtomatik fonli o'rnatish (Dropper / Silent Install)"
    ],
    dangerousPermissions: [
      "android.permission.BIND_DEVICE_ADMIN",
      "android.permission.REQUEST_INSTALL_PACKAGES",
      "android.permission.RECEIVE_BOOT_COMPLETED"
    ],
    isInstalled: false,
    isSystem: false
  },
  {
    id: "apk-4",
    name: "Calculator_Safe.apk",
    packageName: "com.calculator.simple.clean",
    version: "1.2.0",
    size: "3.2 MB",
    sha256: "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
    threatScore: 2,
    severity: "SAFE",
    reasons: ["Toza ilova, hech qanday ortiqcha ruxsatlar talab qilinmagan"],
    dangerousPermissions: [],
    isInstalled: false,
    isSystem: false
  },
  {
    id: "apk-5",
    name: "Telegram_Official.apk",
    packageName: "org.telegram.messenger",
    version: "10.9.2",
    size: "44.2 MB",
    sha256: "5e8b9a1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a",
    threatScore: 8,
    severity: "SAFE",
    reasons: ["Rasmiy raqamli imzo tekshirildi, xavfli anomaliyalar yo'q"],
    dangerousPermissions: [],
    isInstalled: true,
    isSystem: false
  }
];

export default function App() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'scanner' | 'audit' | 'lab' | 'code'>('dashboard');
  const [apks, setApks] = useState<ApkItem[]>(INITIAL_APKS);
  const [malwareDb, setMalwareDb] = useState<MalwareSignature[]>(INITIAL_MALWARE_DB);
  const [isRealtimeActive, setIsRealtimeActive] = useState<boolean>(true);
  const [isAccessibilityActive, setIsAccessibilityActive] = useState<boolean>(true);
  const [isScanning, setIsScanning] = useState<boolean>(false);
  const [scanProgress, setScanProgress] = useState<number>(0);
  const [selectedApk, setSelectedApk] = useState<ApkItem | null>(null);
  const [blockedAlert, setBlockedAlert] = useState<{ appName: string; reason: string; packageName: string } | null>(null);
  const [notificationMsg, setNotificationMsg] = useState<string | null>(null);
  const [blockedCount, setBlockedCount] = useState<number>(3);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filterSeverity, setFilterSeverity] = useState<string>('ALL');

  // New hash state
  const [newHash, setNewHash] = useState('');
  const [newName, setNewName] = useState('');
  const [newCategory, setNewCategory] = useState('Banking Trojan');

  const showToast = (msg: string) => {
    setNotificationMsg(msg);
    setTimeout(() => setNotificationMsg(null), 3000);
  };

  const runScan = () => {
    if (isScanning) return;
    setIsScanning(true);
    setScanProgress(10);
    const interval = setInterval(() => {
      setScanProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setIsScanning(false);
          showToast(`Skanerlash tugadi: ${apks.length} ta APK tekshirildi!`);
          return 100;
        }
        return prev + 18;
      });
    }, 200);
  };

  const triggerSimulation = () => {
    const malicious = apks.find((a) => a.threatScore >= 80) || apks[0];
    setBlockedAlert({
      appName: malicious.name,
      packageName: malicious.packageName,
      reason: malicious.reasons[0] || "Zararli ruxsatlar aniqlandi"
    });
    setBlockedCount((c) => c + 1);
  };

  const handleDeleteApk = (id: string, name: string) => {
    setApks((prev) => prev.filter((a) => a.id !== id));
    if (selectedApk?.id === id) setSelectedApk(null);
    showToast(`"${name}" o'chirildi!`);
  };

  const handleAddHash = () => {
    if (!newHash.trim() || !newName.trim()) {
      showToast("Xesh va nomni to'ldiring!");
      return;
    }
    const signature: MalwareSignature = {
      hash: newHash.trim(),
      name: newName.trim(),
      category: newCategory,
      description: "Qo'lda kiritilgan virus imzosi"
    };
    setMalwareDb([signature, ...malwareDb]);
    setNewHash('');
    setNewName('');
    showToast(`Yangi virus imzosi (${signature.name}) qo'shildi!`);
  };

  const criticalCount = apks.filter((a) => a.severity === 'CRITICAL' || a.severity === 'HIGH').length;

  return (
    <div className="w-full min-h-screen bg-[#070B14] text-slate-100 flex flex-col font-sans select-none pb-16">
      {/* Simulated Android Status Bar */}
      <div className="w-full bg-[#0D1526] px-4 py-1.5 flex items-center justify-between text-[11px] text-slate-400 font-medium border-b border-[#1E293B]">
        <div className="flex items-center gap-1.5 font-semibold text-slate-300">
          <span>10:42</span>
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse ml-1" />
        </div>
        <div className="flex items-center gap-2 text-slate-400">
          <span className="text-[10px] text-emerald-400 font-bold">QALQON FAOL</span>
          <Signal className="w-3.5 h-3.5" />
          <Wifi className="w-3.5 h-3.5" />
          <div className="flex items-center gap-0.5">
            <span>100%</span>
            <Battery className="w-3.5 h-3.5 text-emerald-400" />
          </div>
        </div>
      </div>

      {/* Android App Top Header */}
      <header className="bg-[#0F172A] border-b border-[#1E293B] px-4 py-3 sticky top-0 z-30 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-[#00E5FF]/15 border border-[#00E5FF]/50 flex items-center justify-center text-[#00E5FF] shadow-[0_0_12px_rgba(0,229,255,0.3)]">
            <Shield className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-extrabold text-white tracking-wide flex items-center gap-1.5">
              Apk Soqchi
              <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                PRO
              </span>
            </h1>
            <p className="text-[11px] text-slate-400">Real-vaqt APK Qalqoni & Antivirus</p>
          </div>
        </div>

        <button
          onClick={() => setActiveTab('code')}
          className="px-2.5 py-1.5 rounded-lg bg-[#1E293B] hover:bg-[#334155] text-slate-300 text-xs font-semibold flex items-center gap-1 border border-[#334155]"
          title="Android Kotlin kodini ko'rish"
        >
          <FileCode className="w-3.5 h-3.5 text-[#00E5FF]" />
          <span>Kodlar</span>
        </button>
      </header>

      {/* Floating Notification */}
      {notificationMsg && (
        <div className="fixed top-14 left-4 right-4 z-50 bg-[#1E293B] border border-[#00E5FF] text-white px-4 py-2.5 rounded-xl shadow-2xl flex items-center gap-2 text-xs animate-in fade-in slide-in-from-top duration-200">
          <CheckCircle2 className="w-4 h-4 text-[#00E5FF] shrink-0" />
          <span className="font-medium">{notificationMsg}</span>
        </div>
      )}

      {/* Main Content Viewport */}
      <div className="flex-1 w-full max-w-md mx-auto p-3.5 space-y-4">
        {/* TAB 1: DASHBOARD */}
        {activeTab === 'dashboard' && (
          <div className="space-y-4">
            {/* Big Interactive Cyber Shield Card */}
            <div className="rounded-2xl bg-gradient-to-b from-[#131F37] to-[#0A1224] border border-[#1E293B] p-5 text-center relative overflow-hidden shadow-xl">
              <div className="flex justify-center mb-3">
                <div className="relative w-28 h-28 flex items-center justify-center">
                  <div
                    className={`absolute inset-0 rounded-full ${
                      criticalCount > 0 ? 'bg-rose-500/10' : 'bg-[#00E5FF]/10'
                    } animate-ping`}
                  />
                  <div
                    className={`w-24 h-24 rounded-full bg-[#0D172A] border-2 ${
                      criticalCount > 0
                        ? 'border-rose-500 shadow-[0_0_25px_rgba(244,63,94,0.35)]'
                        : 'border-[#00E5FF] shadow-[0_0_25px_rgba(0,229,255,0.35)]'
                    } flex items-center justify-center`}
                  >
                    {criticalCount > 0 ? (
                      <ShieldAlert className="w-12 h-12 text-rose-500 animate-pulse" />
                    ) : (
                      <ShieldCheck className="w-12 h-12 text-[#00E5FF]" />
                    )}
                  </div>
                </div>
              </div>

              <h2 className="text-lg font-black text-white">
                {criticalCount > 0 ? "DIQQAT: Xavfli APK Topildi!" : "Qurilmangiz To'liq Himoyalangan"}
              </h2>
              <p className="text-xs text-slate-300 mt-1 max-w-xs mx-auto">
                {criticalCount > 0
                  ? "Xotirada zararli ruxsatlarga ega APK fayllar aniqlandi. Ularni darhol o'chiring."
                  : "Accessibility Qalqoni faol: har qanday yangi o'rnatish avtomatik tekshiriladi."}
              </p>

              {/* Real-time scan progress bar */}
              {isScanning && (
                <div className="mt-4 pt-3 border-t border-[#1E293B]">
                  <div className="flex justify-between text-[11px] text-slate-400 mb-1">
                    <span>Skanerlanmoqda...</span>
                    <span className="text-[#00E5FF] font-bold">{scanProgress}%</span>
                  </div>
                  <div className="w-full bg-[#1E293B] h-2 rounded-full overflow-hidden">
                    <div
                      className="bg-gradient-to-r from-[#00E5FF] to-emerald-400 h-full transition-all duration-200"
                      style={{ width: `${scanProgress}%` }}
                    />
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="grid grid-cols-1 gap-2.5 mt-4">
                <button
                  onClick={runScan}
                  disabled={isScanning}
                  className="w-full py-3 rounded-xl bg-[#00E5FF] hover:bg-[#00c9e0] text-[#0A0F1D] font-extrabold text-xs flex items-center justify-center gap-2 transition shadow-[0_0_15px_rgba(0,229,255,0.3)] disabled:opacity-50"
                >
                  <Play className="w-4 h-4 fill-current" />
                  {isScanning ? "Skanerlash Jarayonda..." : "⚡ XOTIRANI CHUQUR SKANERLASH"}
                </button>

                <button
                  onClick={triggerSimulation}
                  className="w-full py-2.5 rounded-xl bg-rose-500/15 border border-rose-500/40 hover:bg-rose-500/25 text-rose-400 font-bold text-xs flex items-center justify-center gap-2 transition"
                >
                  <AlertTriangle className="w-4 h-4" />
                  Zararli O'rnatishni Sinash (Demo Bloklash)
                </button>
              </div>
            </div>

            {/* Matrix Stats */}
            <div className="grid grid-cols-3 gap-2">
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B] text-center">
                <div className="text-[10px] text-slate-400">Tekshirildi</div>
                <div className="text-lg font-black text-white mt-0.5">{apks.length}</div>
                <div className="text-[9px] text-emerald-400 font-bold">APK fayl</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B] text-center">
                <div className="text-[10px] text-slate-400">Tahdidlar</div>
                <div className="text-lg font-black text-rose-400 mt-0.5">{criticalCount}</div>
                <div className="text-[9px] text-rose-400 font-bold">Xavfli APK</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B] text-center">
                <div className="text-[10px] text-slate-400">Bloklandi</div>
                <div className="text-lg font-black text-[#00E5FF] mt-0.5">{blockedCount}</div>
                <div className="text-[9px] text-slate-400 font-bold">O'rnatish</div>
              </div>
            </div>

            {/* Quick Protection Toggles */}
            <div className="p-3.5 rounded-xl bg-[#0F172A] border border-[#1E293B] space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-xs font-bold text-white">Real-vaqtda O'rnatish Qalqoni</div>
                  <div className="text-[10px] text-slate-400">APK ochilganda avtomatik tahlil</div>
                </div>
                <button
                  onClick={() => setIsRealtimeActive(!isRealtimeActive)}
                  className={`w-11 h-6 rounded-full transition p-1 flex items-center ${
                    isRealtimeActive ? 'bg-emerald-500 justify-end' : 'bg-slate-700 justify-start'
                  }`}
                >
                  <div className="w-4 h-4 rounded-full bg-white shadow-md" />
                </button>
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-[#1E293B]">
                <div>
                  <div className="text-xs font-bold text-white">Accessibility Bloklovchi</div>
                  <div className="text-[10px] text-slate-400">Cancel tugmasini avtomatik bosish</div>
                </div>
                <button
                  onClick={() => setIsAccessibilityActive(!isAccessibilityActive)}
                  className={`w-11 h-6 rounded-full transition p-1 flex items-center ${
                    isAccessibilityActive ? 'bg-[#00E5FF] justify-end' : 'bg-slate-700 justify-start'
                  }`}
                >
                  <div className="w-4 h-4 rounded-full bg-white shadow-md" />
                </button>
              </div>
            </div>

            {/* Dangerous APK Feed */}
            <div className="space-y-2">
              <div className="flex items-center justify-between px-1">
                <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                  <Flame className="w-3.5 h-3.5 text-rose-400" />
                  Topilgan Shubhali APKlar
                </span>
                <button onClick={() => setActiveTab('scanner')} className="text-[11px] text-[#00E5FF]">
                  Barchasi &rarr;
                </button>
              </div>

              {apks
                .filter((a) => a.threatScore >= 60)
                .map((apk) => (
                  <div
                    key={apk.id}
                    onClick={() => setSelectedApk(apk)}
                    className="p-3 rounded-xl bg-[#0F172A] border border-rose-500/30 hover:border-rose-500/60 transition cursor-pointer flex items-center justify-between"
                  >
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-lg bg-rose-500/20 text-rose-400 flex items-center justify-center font-bold text-xs shrink-0">
                        <AlertTriangle className="w-4 h-4" />
                      </div>
                      <div className="overflow-hidden">
                        <div className="text-xs font-bold text-white truncate max-w-[170px]">{apk.name}</div>
                        <div className="text-[10px] text-rose-300/80 truncate max-w-[170px]">{apk.reasons[0]}</div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <span className="text-[11px] font-black text-rose-400 bg-rose-500/20 px-2 py-0.5 rounded">
                        {apk.threatScore} ball
                      </span>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteApk(apk.id, apk.name);
                        }}
                        className="p-1.5 rounded-lg bg-rose-500/20 text-rose-400 hover:bg-rose-500/40"
                        title="O'chirish"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {/* TAB 2: SCANNER */}
        {activeTab === 'scanner' && (
          <div className="space-y-3.5">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-base font-bold text-white flex items-center gap-1.5">
                  <Search className="w-4 h-4 text-[#00E5FF]" />
                  Xotiradagi APK Skaneri
                </h2>
                <p className="text-[11px] text-slate-400">Downloads va xotiradagi barcha paketlar</p>
              </div>
              <button
                onClick={runScan}
                disabled={isScanning}
                className="px-3 py-1.5 rounded-lg bg-[#00E5FF] text-[#0A0F1D] text-xs font-bold flex items-center gap-1"
              >
                <RotateCcw className="w-3 h-3" />
                Yangilash
              </button>
            </div>

            {/* Filter Buttons */}
            <div className="flex gap-1.5">
              {['ALL', 'CRITICAL', 'SAFE'].map((f) => (
                <button
                  key={f}
                  onClick={() => setFilterSeverity(f)}
                  className={`flex-1 py-1.5 rounded-lg text-[11px] font-bold transition ${
                    filterSeverity === f
                      ? 'bg-[#00E5FF] text-[#0A0F1D]'
                      : 'bg-[#0F172A] text-slate-400 border border-[#1E293B]'
                  }`}
                >
                  {f === 'ALL' && `Barchasi (${apks.length})`}
                  {f === 'CRITICAL' && `Xavflilar (${criticalCount})`}
                  {f === 'SAFE' && `Tozalar (${apks.length - criticalCount})`}
                </button>
              ))}
            </div>

            {/* APK Cards */}
            <div className="space-y-2.5">
              {apks
                .filter((apk) => {
                  if (filterSeverity === 'CRITICAL') return apk.threatScore >= 60;
                  if (filterSeverity === 'SAFE') return apk.threatScore < 60;
                  return true;
                })
                .map((apk) => {
                  const isDangerous = apk.threatScore >= 60;
                  return (
                    <div
                      key={apk.id}
                      onClick={() => setSelectedApk(apk)}
                      className={`p-3.5 rounded-xl bg-[#0F172A] border transition cursor-pointer ${
                        isDangerous
                          ? 'border-rose-500/40 hover:border-rose-500'
                          : 'border-[#1E293B] hover:border-[#00E5FF]/40'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-start gap-2.5">
                          <div
                            className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                              isDangerous
                                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                                : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                            }`}
                          >
                            {isDangerous ? <AlertTriangle className="w-5 h-5" /> : <CheckCircle2 className="w-5 h-5" />}
                          </div>
                          <div>
                            <div className="text-xs font-bold text-white truncate max-w-[190px]">{apk.name}</div>
                            <div className="text-[10px] text-slate-400 truncate max-w-[190px]">{apk.packageName}</div>
                            <div className="text-[9px] text-slate-500 mt-0.5">
                              {apk.size} • v{apk.version}
                            </div>
                          </div>
                        </div>

                        <div className="flex flex-col items-end gap-1.5">
                          <span
                            className={`text-[10px] font-black px-2 py-0.5 rounded ${
                              isDangerous
                                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                                : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                            }`}
                          >
                            {apk.threatScore}/100
                          </span>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteApk(apk.id, apk.name);
                            }}
                            className="p-1 rounded text-slate-400 hover:text-rose-400"
                            title="O'chirish"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {apk.dangerousPermissions.length > 0 && (
                        <div className="mt-2 pt-2 border-t border-[#1E293B] flex flex-wrap gap-1">
                          {apk.dangerousPermissions.map((perm) => (
                            <span
                              key={perm}
                              className="text-[9px] px-1.5 py-0.2 rounded bg-rose-500/15 text-rose-300 font-mono"
                            >
                              {perm.replace('android.permission.', '')}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
            </div>
          </div>
        )}

        {/* TAB 3: AUDIT */}
        {activeTab === 'audit' && (
          <div className="space-y-3.5">
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-1.5">
                <Smartphone className="w-4 h-4 text-[#00E5FF]" />
                O'rnatilgan Ilovalar Auditi
              </h2>
              <p className="text-[11px] text-slate-400">Ruxsatlar va maxfiylik xatarlari tahlili</p>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="text-[10px] text-slate-400">SMS O'qish Ruxsati</div>
                <div className="text-base font-bold text-amber-400 mt-0.5">2 ta ilova</div>
                <div className="text-[9px] text-slate-500">OTP kodlarni ko'rish xavfi</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="text-[10px] text-slate-400">Overlay (Oyna)</div>
                <div className="text-base font-bold text-rose-400 mt-0.5">1 ta ilova</div>
                <div className="text-[9px] text-slate-500">Phishing qilish imkoni</div>
              </div>
            </div>

            <div className="space-y-2">
              {apks.map((app) => (
                <div
                  key={app.id}
                  onClick={() => setSelectedApk(app)}
                  className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B] hover:border-[#00E5FF]/40 cursor-pointer flex items-center justify-between"
                >
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-lg bg-[#1E293B] flex items-center justify-center text-slate-300">
                      <Smartphone className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-white truncate max-w-[170px]">{app.name}</div>
                      <div className="text-[10px] text-slate-400 truncate max-w-[170px]">{app.packageName}</div>
                    </div>
                  </div>

                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                      app.threatScore >= 60
                        ? 'bg-rose-500/20 text-rose-400'
                        : 'bg-emerald-500/20 text-emerald-400'
                    }`}
                  >
                    {app.threatScore >= 60 ? 'Shubhali' : 'Xavfsiz'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 4: LAB */}
        {activeTab === 'lab' && (
          <div className="space-y-3.5">
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-1.5">
                <Radio className="w-4 h-4 text-[#00E5FF]" />
                Kiber Lab & Qora Ro'yxat
              </h2>
              <p className="text-[11px] text-slate-400">Lokal Room bazasidagi viruslar xesh ro'yxati</p>
            </div>

            {/* Add Hash */}
            <div className="p-3.5 rounded-xl bg-[#0F172A] border border-[#1E293B] space-y-2.5">
              <div className="text-xs font-bold text-white flex items-center gap-1.5">
                <Plus className="w-3.5 h-3.5 text-[#00E5FF]" />
                Yangi Virus Imzosini Kiritish
              </div>
              <input
                type="text"
                placeholder="Virus nomi (masalan: Cerberus.Banker)"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="w-full bg-[#1E293B] border border-[#334155] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#00E5FF]"
              />
              <input
                type="text"
                placeholder="SHA-256 Hash"
                value={newHash}
                onChange={(e) => setNewHash(e.target.value)}
                className="w-full bg-[#1E293B] border border-[#334155] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-[#00E5FF]"
              />
              <button
                onClick={handleAddHash}
                className="w-full py-2 rounded-lg bg-[#00E5FF] text-[#0A0F1D] text-xs font-bold flex items-center justify-center gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                Bazaga Qo'shish
              </button>
            </div>

            {/* List */}
            <div className="space-y-2">
              <div className="text-xs font-bold text-slate-300">Qora Ro'yxatdagi Viruslar ({malwareDb.length} ta)</div>
              {malwareDb.map((m) => (
                <div key={m.hash} className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B] text-xs space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-white">{m.name}</span>
                    <span className="text-[9px] px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 font-bold">
                      {m.category}
                    </span>
                  </div>
                  <p className="text-[10px] text-slate-400">{m.description}</p>
                  <p className="text-[9px] text-slate-500 font-mono truncate">SHA256: {m.hash}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 5: KOTLIN CODE ARCHITECTURE */}
        {activeTab === 'code' && (
          <div className="space-y-3.5">
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-1.5">
                <FileCode className="w-4 h-4 text-[#00E5FF]" />
                Android Kotlin Modullari (5 Bosqich)
              </h2>
              <p className="text-[11px] text-slate-400">
                Ushbu loyiha papkasida to'liq yaratilgan haqiqiy Android fayllari
              </p>
            </div>

            <div className="space-y-3 text-xs">
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="font-bold text-[#00E5FF]">1. AndroidManifest.xml & Accessibility XML</div>
                <p className="text-[11px] text-slate-300 mt-1">
                  `BIND_ACCESSIBILITY_SERVICE`, `QUERY_ALL_PACKAGES` va `MANAGE_EXTERNAL_STORAGE` ruxsatlari.
                </p>
                <div className="text-[10px] text-slate-500 font-mono mt-1">Joylashuvi: app/src/main/AndroidManifest.xml</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="font-bold text-[#00E5FF]">2. ApkInstallGuardService.kt (Interceptor)</div>
                <p className="text-[11px] text-slate-300 mt-1">
                  Package Installer darchasini tutib olib, xavf aniqlanganda 'Cancel' / 'Bekor qilish' tugmasini bosuvchi kod.
                </p>
                <div className="text-[10px] text-slate-500 font-mono mt-1">Joylashuvi: security/ApkInstallGuardService.kt</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="font-bold text-[#00E5FF]">3. ApkAnalyzer.kt (Scoring Engine)</div>
                <p className="text-[11px] text-slate-300 mt-1">
                  SHA-256 xesh, Manifest ruxsatlari kombinatsiyasi va xavf darajasini hisoblash algoritmi.
                </p>
                <div className="text-[10px] text-slate-500 font-mono mt-1">Joylashuvi: security/ApkAnalyzer.kt</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="font-bold text-[#00E5FF]">4. StorageApkScanner.kt & Room Database</div>
                <p className="text-[11px] text-slate-300 mt-1">
                  Xotiradan APK larni qidirish, o'chirish va virus imzolari bazasi.
                </p>
                <div className="text-[10px] text-slate-500 font-mono mt-1">Joylashuvi: scanner/StorageApkScanner.kt</div>
              </div>

              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#1E293B]">
                <div className="font-bold text-[#00E5FF]">5. Jetpack Compose UI & ViewModel</div>
                <p className="text-[11px] text-slate-300 mt-1">
                  Material 3 Cyber-Security interfeysi va MVVM holat boshqaruvi.
                </p>
                <div className="text-[10px] text-slate-500 font-mono mt-1">Joylashuvi: MainActivity.kt & ApkGuardViewModel.kt</div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Emergency Blocked Alert Modal */}
      {blockedAlert && (
        <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="max-w-sm w-full bg-[#180A12] border-2 border-rose-500 rounded-3xl p-5 text-center space-y-3 shadow-[0_0_50px_rgba(244,63,94,0.5)] animate-in zoom-in-95 duration-150">
            <div className="w-14 h-14 rounded-full bg-rose-500/20 border-2 border-rose-500 flex items-center justify-center text-rose-500 mx-auto animate-bounce">
              <ShieldAlert className="w-8 h-8" />
            </div>

            <div>
              <h3 className="text-lg font-black text-rose-500">O'RNATISH QAT'IYAN BLOKLANDI!</h3>
              <p className="text-[11px] text-slate-300 mt-1">
                Apk Soqchi Accessibility qalqoni xavfli virusni aniqladi va o'rnatish oynasidagi "Bekor qilish" (Cancel) tugmasini bosdi.
              </p>
            </div>

            <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/40 text-left space-y-1.5 text-xs">
              <div>
                <span className="text-slate-400 text-[10px]">APK nomi: </span>
                <span className="font-bold text-white text-xs">{blockedAlert.appName}</span>
              </div>
              <div>
                <span className="text-slate-400 text-[10px]">Paket: </span>
                <span className="font-mono text-slate-300 text-[10px]">{blockedAlert.packageName}</span>
              </div>
              <div className="pt-1 border-t border-rose-500/30">
                <span className="text-rose-400 font-bold text-[10px]">Sabab: </span>
                <span className="text-rose-200 text-[11px]">{blockedAlert.reason}</span>
              </div>
            </div>

            <button
              onClick={() => setBlockedAlert(null)}
              className="w-full py-2.5 rounded-xl bg-rose-500 hover:bg-rose-600 text-white font-extrabold text-xs transition"
            >
              Tushundim (Telefon Himoyalandi)
            </button>
          </div>
        </div>
      )}

      {/* APK Detail Modal */}
      {selectedApk && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="max-w-sm w-full bg-[#0F172A] border border-[#1E293B] rounded-2xl p-5 space-y-3.5 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-[#1E293B] pb-2.5">
              <div className="flex items-center gap-2">
                <Shield className="w-4 h-4 text-[#00E5FF]" />
                <h3 className="font-bold text-white text-sm">Kiber Tahlil Hisoboti</h3>
              </div>
              <button onClick={() => setSelectedApk(null)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-2 text-xs">
              <div className="p-2.5 bg-[#1E293B] rounded-xl space-y-0.5">
                <div className="font-bold text-white text-xs">{selectedApk.name}</div>
                <div className="text-[10px] text-slate-400">{selectedApk.packageName}</div>
                <div className="text-[10px] text-slate-500">
                  {selectedApk.size} • v{selectedApk.version}
                </div>
              </div>

              <div
                className={`p-2.5 rounded-xl flex items-center justify-between font-bold ${
                  selectedApk.threatScore >= 60
                    ? 'bg-rose-500/15 text-rose-400 border border-rose-500/30'
                    : 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                }`}
              >
                <span>Xavf Darajasi: {selectedApk.severity}</span>
                <span className="text-base font-black">{selectedApk.threatScore}/100</span>
              </div>

              <div className="p-2.5 bg-[#1E293B] rounded-xl space-y-1">
                <div className="text-[10px] text-slate-400 font-semibold">SHA-256 Imzo:</div>
                <div className="font-mono text-[9px] text-[#00E5FF] break-all">{selectedApk.sha256}</div>
              </div>

              <div className="space-y-1">
                <div className="text-[11px] font-bold text-slate-300">Tahlil Xulosasi:</div>
                {selectedApk.reasons.map((r, i) => (
                  <div key={i} className="p-2 rounded bg-rose-950/20 text-rose-300 text-[10px] flex items-center gap-1.5">
                    <AlertTriangle className="w-3 h-3 shrink-0" />
                    <span>{r}</span>
                  </div>
                ))}
              </div>

              {selectedApk.dangerousPermissions.length > 0 && (
                <div className="space-y-1">
                  <div className="text-[11px] font-bold text-slate-300">
                    Xavfli Ruxsatlar ({selectedApk.dangerousPermissions.length}):
                  </div>
                  <div className="space-y-1">
                    {selectedApk.dangerousPermissions.map((p) => (
                      <div key={p} className="text-[10px] text-slate-400 font-mono bg-[#1E293B] p-1.5 rounded">
                        • {p}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="flex gap-2 pt-2 border-t border-[#1E293B]">
              <button
                onClick={() => handleDeleteApk(selectedApk.id, selectedApk.name)}
                className="flex-1 py-2 rounded-xl bg-rose-500/20 border border-rose-500/40 text-rose-400 hover:bg-rose-500/30 text-xs font-bold transition flex items-center justify-center gap-1.5"
              >
                <Trash2 className="w-3.5 h-3.5" />
                O'chirish
              </button>
              <button
                onClick={() => setSelectedApk(null)}
                className="flex-1 py-2 rounded-xl bg-slate-800 text-white text-xs font-bold transition"
              >
                Yopish
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Persistent Android Bottom Navigation Bar */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#0F172A] border-t border-[#1E293B] px-3 py-1.5 flex items-center justify-around max-w-md mx-auto">
        <button
          onClick={() => setActiveTab('dashboard')}
          className={`flex flex-col items-center gap-1 py-1 px-2.5 rounded-xl transition ${
            activeTab === 'dashboard' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Shield className="w-4 h-4" />
          <span className="text-[10px] font-bold">Boshqaruv</span>
        </button>

        <button
          onClick={() => setActiveTab('scanner')}
          className={`flex flex-col items-center gap-1 py-1 px-2.5 rounded-xl transition relative ${
            activeTab === 'scanner' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Search className="w-4 h-4" />
          <span className="text-[10px] font-bold">APK Skaner</span>
          {criticalCount > 0 && (
            <span className="absolute top-0.5 right-1 w-2 h-2 rounded-full bg-rose-500 animate-ping" />
          )}
        </button>

        <button
          onClick={() => setActiveTab('audit')}
          className={`flex flex-col items-center gap-1 py-1 px-2.5 rounded-xl transition ${
            activeTab === 'audit' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Smartphone className="w-4 h-4" />
          <span className="text-[10px] font-bold">Ilovalar</span>
        </button>

        <button
          onClick={() => setActiveTab('lab')}
          className={`flex flex-col items-center gap-1 py-1 px-2.5 rounded-xl transition ${
            activeTab === 'lab' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Radio className="w-4 h-4" />
          <span className="text-[10px] font-bold">Kiber Lab</span>
        </button>
      </nav>
    </div>
  );
}
