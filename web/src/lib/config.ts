/**
 * LootDrop Template Configuration
 *
 * Customize your TWA by editing this single file.
 * All components read from this config — no need to touch individual files.
 */

export const config = {
	/** App identity */
	app: {
		name: 'LootDrop',
		tagline: 'Walk in. Scan. Earn.',
		version: '0.3.0',
		description: 'Location-based crypto rewards powered by Solana.',
		poweredBy: 'Powered by Solana'
	},

	/** Brand colors (Solana palette) */
	colors: {
		primary: '#9945FF',
		primaryLight: '#B58AFF',
		secondary: '#14F195',
		tertiary: '#FFB74D',
		gradient: 'linear-gradient(135deg, #9945FF 0%, #14F195 100%)',
		surface: '#0E0E10',
		themeColor: '#9945FF'
	},

	/** Token display colors */
	tokens: {
		SOL: '#B58AFF',
		USDC: '#14F195',
		BONK: '#FFB74D'
	},

	/** Solana network */
	network: {
		cluster: 'devnet' as 'devnet' | 'mainnet-beta' | 'testnet',
		rpcUrl: 'https://api.devnet.solana.com'
	},

	/** Navigation tabs */
	nav: [
		{ label: 'Map', path: '/', icon: 'map' },
		{ label: 'Scan', path: '/scan', icon: 'scan' },
		{ label: 'Rewards', path: '/rewards', icon: 'gift' },
		{ label: 'Profile', path: '/profile', icon: 'user' }
	],

	/** TWA / Android */
	twa: {
		packageName: 'app.vercel.web_rho_tawny_30.twa',
		hostUrl: 'https://web-rho-tawny-30.vercel.app'
	}
} as const;

/** Helper: get token color from config */
export function tokenColor(token: string): string {
	return config.tokens[token as keyof typeof config.tokens] ?? config.colors.primaryLight;
}
