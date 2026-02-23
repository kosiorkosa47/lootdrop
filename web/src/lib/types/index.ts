/** A reward drop available at a merchant location */
export interface Drop {
	id: string;
	businessName: string;
	businessIcon: string;
	rewardAmount: number;
	rewardToken: 'USDC' | 'SOL' | 'BONK';
	distanceMeters: number;
	expiresAt: string;
	category: DropCategory;
	description: string;
	claimed: boolean;
}

export type DropCategory = 'food' | 'retail' | 'entertainment' | 'fitness' | 'services' | 'crypto';

/** A reward that the user has already claimed */
export interface ClaimedReward {
	id: string;
	businessName: string;
	businessIcon: string;
	amount: number;
	token: 'USDC' | 'SOL' | 'BONK';
	claimedAt: string;
	txHash: string;
}

/** Connected wallet state */
export interface WalletState {
	connected: boolean;
	address: string | null;
	balanceSol: number;
	balanceUsdc: number;
}

/** User profile stats */
export interface UserStats {
	totalEarned: number;
	dropsClaimed: number;
	currentStreak: number;
	memberSince: string;
}

/** App settings */
export interface AppSettings {
	darkMode: boolean;
	notifications: boolean;
}

/** Tab definition for bottom nav */
export interface TabItem {
	id: string;
	label: string;
	icon: string;
	href: string;
}

/** Scan result after QR code scan */
export interface ScanResult {
	success: boolean;
	drop: Drop | null;
	message: string;
}
