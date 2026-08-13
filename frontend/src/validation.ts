export const PARTICIPANT_CODE_PATTERN = /^[A-Z0-9][A-Z0-9_-]{0,39}$/;
export const CONTACT_TIME_OPTIONS = [
  ["09:00-12:00", "오전 9시~12시"],
  ["12:00-15:00", "오후 12시~3시"],
  ["15:00-18:00", "오후 3시~6시"],
  ["18:00-20:00", "오후 6시~8시"]
] as const;
export const PRODUCT_FREQUENCIES = ["매일 1회", "매일 2회 이상", "주 5~6일", "주 2~4일", "주 1일", "월 1~3일", "불규칙", "잘 모르겠음"] as const;
export const MAX_ATTACHMENTS_PER_CATEGORY = 10;
export const ALLOWED_IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

export function todayLocal(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60_000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 10);
}

export function currentMonthLocal(): string {
  return todayLocal().slice(0, 7);
}

export function limitSingleLine(value: string, maxLength: number): string {
  return value.replace(/[\u0000-\u001F\u007F]/g, "").slice(0, maxLength);
}

export function limitMultiline(value: string, maxLength: number): string {
  return value.replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, "").slice(0, maxLength);
}

export function normalizeParticipantCode(value: string): string {
  return value.toUpperCase().replace(/[^A-Z0-9_-]/g, "").slice(0, 40);
}

export function formatKoreanPhone(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  if (digits.startsWith("02")) {
    if (digits.length <= 2) return digits;
    if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`;
    if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
    return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`;
  }
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
}

export function isValidKoreanPhone(value: string): boolean {
  if (!value) return true;
  const digits = value.replace(/\D/g, "");
  return digits.startsWith("02") ? /^02\d{7,8}$/.test(digits) : /^0\d{9,10}$/.test(digits);
}

export function hasAtMostOneDecimal(value: string): boolean {
  return /^\d{1,3}(\.\d)?$/.test(value);
}

export function isIntegerInRange(value: string, min: number, max: number): boolean {
  return /^\d+$/.test(value) && Number(value) >= min && Number(value) <= max;
}

export function ageOnDate(birthDate: string, referenceDate: string): number | null {
  if (!birthDate || !referenceDate) return null;
  const birth = new Date(`${birthDate}T00:00:00`);
  const reference = new Date(`${referenceDate}T00:00:00`);
  if (Number.isNaN(birth.getTime()) || Number.isNaN(reference.getTime()) || birth > reference) return null;
  let age = reference.getFullYear() - birth.getFullYear();
  const beforeBirthday = reference.getMonth() < birth.getMonth() || (reference.getMonth() === birth.getMonth() && reference.getDate() < birth.getDate());
  if (beforeBirthday) age -= 1;
  return age;
}

export function monthMinus(referenceDate: string, months: number): string {
  const reference = new Date(`${referenceDate || todayLocal()}T00:00:00`);
  reference.setMonth(reference.getMonth() - months);
  return `${reference.getFullYear()}-${String(reference.getMonth() + 1).padStart(2, "0")}`;
}
