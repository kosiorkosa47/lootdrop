import type { Drop, ClaimedReward, UserStats } from '$lib/types';

/** Mock nearby drops for the map/home view */
export const MOCK_DROPS: Drop[] = [
	{
		id: 'drop-1',
		businessName: 'Solana Coffee',
		businessIcon: '\u2615',
		rewardAmount: 5,
		rewardToken: 'USDC',
		distanceMeters: 120,
		expiresAt: '2026-02-25T18:00:00Z',
		category: 'food',
		description: 'Free coffee with any pastry purchase',
		claimed: false
	},
	{
		id: 'drop-2',
		businessName: 'CryptoGym Pro',
		businessIcon: '\uD83C\uDFCB\uFE0F',
		rewardAmount: 0.1,
		rewardToken: 'SOL',
		distanceMeters: 340,
		expiresAt: '2026-02-28T23:59:00Z',
		category: 'fitness',
		description: 'First visit bonus - scan at the front desk',
		claimed: false
	},
	{
		id: 'drop-3',
		businessName: 'Pixel Pizza',
		businessIcon: '\uD83C\uDF55',
		rewardAmount: 3,
		rewardToken: 'USDC',
		distanceMeters: 450,
		expiresAt: '2026-02-24T21:00:00Z',
		category: 'food',
		description: '$3 off your next order',
		claimed: false
	},
	{
		id: 'drop-4',
		businessName: 'Web3 Books',
		businessIcon: '\uD83D\uDCDA',
		rewardAmount: 2,
		rewardToken: 'USDC',
		distanceMeters: 680,
		expiresAt: '2026-03-01T12:00:00Z',
		category: 'retail',
		description: 'Earn rewards on any purchase over $10',
		claimed: false
	},
	{
		id: 'drop-5',
		businessName: 'DeFi Donuts',
		businessIcon: '\uD83C\uDF69',
		rewardAmount: 1000000,
		rewardToken: 'BONK',
		distanceMeters: 890,
		expiresAt: '2026-02-26T15:00:00Z',
		category: 'food',
		description: 'BONK bonus - scan at any DeFi Donuts location',
		claimed: false
	},
	{
		id: 'drop-6',
		businessName: 'GameFi Arcade',
		businessIcon: '\uD83C\uDFAE',
		rewardAmount: 10,
		rewardToken: 'USDC',
		distanceMeters: 1200,
		expiresAt: '2026-03-05T20:00:00Z',
		category: 'entertainment',
		description: '$10 free play when you scan at entrance',
		claimed: false
	},
	{
		id: 'drop-7',
		businessName: 'Node Noodles',
		businessIcon: '\uD83C\uDF5C',
		rewardAmount: 0.05,
		rewardToken: 'SOL',
		distanceMeters: 1500,
		expiresAt: '2026-02-27T22:00:00Z',
		category: 'food',
		description: 'Lunch special reward - weekdays only',
		claimed: false
	},
	{
		id: 'drop-8',
		businessName: 'Validator Cuts',
		businessIcon: '\u2702\uFE0F',
		rewardAmount: 7,
		rewardToken: 'USDC',
		distanceMeters: 2100,
		expiresAt: '2026-03-10T19:00:00Z',
		category: 'services',
		description: 'First haircut reward - new customers',
		claimed: false
	},
	{
		id: 'drop-9',
		businessName: 'Block Brew',
		businessIcon: '\uD83C\uDF7A',
		rewardAmount: 4,
		rewardToken: 'USDC',
		distanceMeters: 2500,
		expiresAt: '2026-02-28T02:00:00Z',
		category: 'food',
		description: 'Happy hour check-in reward',
		claimed: false
	},
	{
		id: 'drop-10',
		businessName: 'Mint Fitness',
		businessIcon: '\uD83E\uDDD8',
		rewardAmount: 0.15,
		rewardToken: 'SOL',
		distanceMeters: 3400,
		expiresAt: '2026-03-15T08:00:00Z',
		category: 'fitness',
		description: 'Weekly yoga class check-in bonus',
		claimed: false
	}
];

/** Mock claimed rewards history */
export const MOCK_CLAIMED_REWARDS: ClaimedReward[] = [
	{
		id: 'claim-1',
		businessName: 'Solana Coffee',
		businessIcon: '\u2615',
		amount: 5,
		token: 'USDC',
		claimedAt: '2026-02-22T14:30:00Z',
		txHash: '3xK9mZ...vRt7pQ'
	},
	{
		id: 'claim-2',
		businessName: 'CryptoGym Pro',
		businessIcon: '\uD83C\uDFCB\uFE0F',
		amount: 0.1,
		token: 'SOL',
		claimedAt: '2026-02-21T09:15:00Z',
		txHash: '7nB2cX...wLm4kJ'
	},
	{
		id: 'claim-3',
		businessName: 'DeFi Donuts',
		businessIcon: '\uD83C\uDF69',
		amount: 500000,
		token: 'BONK',
		claimedAt: '2026-02-20T16:45:00Z',
		txHash: '9pR5hY...tNq8zM'
	},
	{
		id: 'claim-4',
		businessName: 'Pixel Pizza',
		businessIcon: '\uD83C\uDF55',
		amount: 3,
		token: 'USDC',
		claimedAt: '2026-02-18T12:00:00Z',
		txHash: '2mF6dA...xVs3bW'
	},
	{
		id: 'claim-5',
		businessName: 'GameFi Arcade',
		businessIcon: '\uD83C\uDFAE',
		amount: 10,
		token: 'USDC',
		claimedAt: '2026-02-15T19:20:00Z',
		txHash: '5jT8gC...yHp1nE'
	}
];

/** Mock user stats */
export const MOCK_USER_STATS: UserStats = {
	totalEarned: 42.5,
	dropsClaimed: 12,
	currentStreak: 5,
	memberSince: '2026-01-15T00:00:00Z'
};
