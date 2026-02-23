import type { WalletState, AppSettings, Drop, ClaimedReward, UserStats } from '$lib/types';
import { MOCK_DROPS, MOCK_CLAIMED_REWARDS, MOCK_USER_STATS } from '$lib/data/mock';

/**
 * Reactive app state using Svelte 5 runes.
 * State is wrapped in objects so we can mutate properties
 * without reassigning the top-level $state (which is not
 * allowed in exported module-level state).
 */

// Root state container - single object, never reassigned, only mutated
const appState = $state({
	wallet: {
		connected: false,
		address: null as string | null,
		balanceSol: 0,
		balanceUsdc: 0
	} satisfies WalletState,

	settings: {
		darkMode: true,
		notifications: true
	} satisfies AppSettings,

	drops: [...MOCK_DROPS] as Drop[],
	claimedRewards: [...MOCK_CLAIMED_REWARDS] as ClaimedReward[],

	userStats: { ...MOCK_USER_STATS } as UserStats,

	splashVisible: true,
	isRefreshing: false
});

// Exported read-only derived accessors
export function getWallet(): WalletState {
	return appState.wallet;
}

export function getSettings(): AppSettings {
	return appState.settings;
}

export function getDrops(): Drop[] {
	return appState.drops;
}

export function getClaimedRewards(): ClaimedReward[] {
	return appState.claimedRewards;
}

export function getUserStats(): UserStats {
	return appState.userStats;
}

export function isSplashVisible(): boolean {
	return appState.splashVisible;
}

export function getIsRefreshing(): boolean {
	return appState.isRefreshing;
}

/** Connect a mock wallet */
export function connectWallet(): void {
	const mockAddress = '7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU';
	appState.wallet.connected = true;
	appState.wallet.address = mockAddress;
	appState.wallet.balanceSol = 2.45;
	appState.wallet.balanceUsdc = 128.50;
}

/** Disconnect wallet */
export function disconnectWallet(): void {
	appState.wallet.connected = false;
	appState.wallet.address = null;
	appState.wallet.balanceSol = 0;
	appState.wallet.balanceUsdc = 0;
}

/** Truncate a wallet address for display */
export function truncateAddress(address: string): string {
	return `${address.slice(0, 4)}...${address.slice(-4)}`;
}

/** Claim a drop reward */
export function claimDrop(dropId: string): ClaimedReward | null {
	const dropIndex = appState.drops.findIndex((d) => d.id === dropId);
	if (dropIndex === -1 || appState.drops[dropIndex].claimed) return null;

	const drop = appState.drops[dropIndex];
	appState.drops[dropIndex].claimed = true;

	const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	const hash = Array.from({ length: 6 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');

	const reward: ClaimedReward = {
		id: `claim-${Date.now()}`,
		businessName: drop.businessName,
		businessIcon: drop.businessIcon,
		amount: drop.rewardAmount,
		token: drop.rewardToken,
		claimedAt: new Date().toISOString(),
		txHash: `${hash}...${hash}`
	};

	appState.claimedRewards.unshift(reward);
	appState.userStats.totalEarned += drop.rewardToken === 'USDC' ? drop.rewardAmount : 0;
	appState.userStats.dropsClaimed += 1;

	return reward;
}

/** Simulate pull-to-refresh */
export async function refreshDrops(): Promise<void> {
	appState.isRefreshing = true;
	await new Promise((resolve) => setTimeout(resolve, 1200));
	// Shuffle distances slightly to simulate location updates
	for (let i = 0; i < appState.drops.length; i++) {
		appState.drops[i].distanceMeters = Math.max(
			50,
			appState.drops[i].distanceMeters + Math.floor(Math.random() * 200 - 100)
		);
	}
	appState.isRefreshing = false;
}

/** Toggle dark mode */
export function toggleDarkMode(): void {
	appState.settings.darkMode = !appState.settings.darkMode;
	if (typeof document !== 'undefined') {
		document.body.classList.toggle('light-mode', !appState.settings.darkMode);
	}
}

/** Toggle notifications */
export function toggleNotifications(): void {
	appState.settings.notifications = !appState.settings.notifications;
}

/** Dismiss splash screen */
export function dismissSplash(): void {
	appState.splashVisible = false;
}

/** Format distance for display */
export function formatDistance(meters: number): string {
	if (meters < 1000) return `${meters}m`;
	return `${(meters / 1000).toFixed(1)}km`;
}

/** Format token amount for display */
export function formatTokenAmount(amount: number, token: string): string {
	if (token === 'BONK') {
		if (amount >= 1000000) return `${(amount / 1000000).toFixed(1)}M`;
		if (amount >= 1000) return `${(amount / 1000).toFixed(0)}K`;
	}
	return amount.toString();
}

/** Format relative time */
export function formatTimeAgo(isoDate: string): string {
	const diff = Date.now() - new Date(isoDate).getTime();
	const hours = Math.floor(diff / 3600000);
	const days = Math.floor(hours / 24);

	if (days > 0) return `${days}d ago`;
	if (hours > 0) return `${hours}h ago`;
	return 'Just now';
}

/** Format expiry time */
export function formatExpiry(isoDate: string): string {
	const diff = new Date(isoDate).getTime() - Date.now();
	if (diff <= 0) return 'Expired';

	const hours = Math.floor(diff / 3600000);
	const days = Math.floor(hours / 24);

	if (days > 0) return `${days}d left`;
	return `${hours}h left`;
}
