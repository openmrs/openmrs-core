const VERSION = 1;
const SALT_LENGTH = 16;
const IV_LENGTH = 12;
const KEY_LENGTH = 32;
const TIMESTAMP_LENGTH = 13;
const CHECKSUM_LENGTH = 32;

const encoder = new TextEncoder();
const decoder = new TextDecoder('utf-8', { fatal: true });

const PasswordEncryption = {
  async encrypt(password) {
    const salt = crypto.getRandomValues(new Uint8Array(SALT_LENGTH));
    const timestampStr = Date.now().toString();
    const timestampBytes = encoder.encode(timestampStr);
    const passwordBytes = encoder.encode(password);
    const checksumInput = new Uint8Array([...salt, ...timestampBytes, ...passwordBytes]);
    const checksum = new Uint8Array(await crypto.subtle.digest('SHA-256', checksumInput));

    const combined = new Uint8Array([...salt, ...timestampBytes, ...passwordBytes, ...checksum]);
    const iv = crypto.getRandomValues(new Uint8Array(IV_LENGTH));
    const key = crypto.getRandomValues(new Uint8Array(KEY_LENGTH));

    const cryptoKey = await crypto.subtle.importKey('raw', key, 'AES-GCM', false, ['encrypt']);
    const encrypted = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, cryptoKey, combined));

    const final = new Uint8Array([VERSION, ...iv, ...encrypted, ...key]);
    return btoa(String.fromCharCode(...final));
  },

  async decrypt() {
    const base64 = sessionStorage.getItem('databaseRootPassword');
    if (!base64) throw new Error('No encrypted password found.');

    const raw = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
    const version = raw[0];
    if (version !== VERSION) throw new Error('Unsupported encryption version.');

    const iv = raw.slice(1, 1 + IV_LENGTH);
    const key = raw.slice(-KEY_LENGTH);
    const encrypted = raw.slice(1 + IV_LENGTH, -KEY_LENGTH);

    const cryptoKey = await crypto.subtle.importKey('raw', key, 'AES-GCM', false, ['decrypt']);
    let decrypted;
    try {
      decrypted = new Uint8Array(await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, cryptoKey, encrypted));
    } catch (e) {
      throw new Error('Decryption failed. Possibly tampered or expired data.');
    }

    const salt = decrypted.slice(0, SALT_LENGTH);
    const timestampBytes = decrypted.slice(SALT_LENGTH, SALT_LENGTH + TIMESTAMP_LENGTH);
    const passwordBytes = decrypted.slice(SALT_LENGTH + TIMESTAMP_LENGTH, -CHECKSUM_LENGTH);
    const storedChecksum = decrypted.slice(-CHECKSUM_LENGTH);

    const checksumInput = new Uint8Array([...salt, ...timestampBytes, ...passwordBytes]);
    const computedChecksum = new Uint8Array(await crypto.subtle.digest('SHA-256', checksumInput));

    if (!storedChecksum.every((b, i) => b === computedChecksum[i])) {
      throw new Error('Checksum mismatch. Data may be corrupted.');
    }

    const timestamp = parseInt(decoder.decode(timestampBytes));
    if (Date.now() - timestamp > 5 * 60 * 1000) {
      throw new Error('Encrypted data has expired.');
    }

    return decoder.decode(passwordBytes);
  },

  clear() {
    sessionStorage.removeItem('databaseRootPassword');
  }
}; 