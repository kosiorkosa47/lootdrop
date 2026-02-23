<script lang="ts">
	import WalletButton from '$lib/components/WalletButton.svelte';
	import { config } from '$lib/config';
	import {
		getWallet,
		getUserStats,
		getSettings,
		truncateAddress,
		toggleDarkMode,
		toggleNotifications,
		disconnectWallet
	} from '$lib/stores/app.svelte';

	const darkMode = $derived(getSettings().darkMode);
	const notifications = $derived(getSettings().notifications);
	const wallet = $derived(getWallet());
	const stats = $derived(getUserStats());

	function formatMemberSince(isoDate: string): string {
		return new Date(isoDate).toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		});
	}
</script>

<svelte:head>
	<title>LootDrop - Profile</title>
</svelte:head>

<div class="px-4 pt-4">
	<!-- MD3 Top App Bar (Large) -->
	<div class="mb-6">
		<p class="text-sm font-medium mb-1" style="color: var(--md-sys-color-on-surface-variant);">
			Wallet & settings
		</p>
		<h1 class="text-3xl font-normal tracking-tight" style="color: var(--md-sys-color-on-surface);">
			Profile
		</h1>
	</div>

	<!-- Wallet section — MD3 Outlined Card -->
	<div
		class="mb-6 p-5"
		style="
			animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1);
			border-radius: var(--md-sys-shape-corner-medium);
			border: 1px solid var(--md-sys-color-outline-variant);
		"
	>
		{#if wallet.connected && wallet.address}
			<div class="flex items-center gap-3 mb-4">
				<div
					class="w-12 h-12 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-primary-container);"
				>
					<svg class="w-6 h-6" style="color: var(--md-sys-color-on-primary-container);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<rect x="2" y="6" width="20" height="12" rx="2" />
						<path d="M2 10h20" />
					</svg>
				</div>
				<div class="flex-1">
					<p class="text-xs" style="color: var(--md-sys-color-on-surface-variant);">Connected Wallet</p>
					<p class="font-mono text-sm font-medium" style="color: var(--md-sys-color-on-surface);">
						{truncateAddress(wallet.address)}
					</p>
				</div>
				<div class="w-3 h-3 rounded-full" style="background-color: #14F195;" title="Connected"></div>
			</div>

			<!-- Balance cards -->
			<div class="grid grid-cols-2 gap-3 mb-4">
				<div
					class="p-3"
					style="
						border-radius: var(--md-sys-shape-corner-small);
						background-color: var(--md-sys-color-surface-container-high);
					"
				>
					<p class="text-xs" style="color: var(--md-sys-color-on-surface-variant);">SOL</p>
					<p class="font-semibold" style="color: var(--md-sys-color-tertiary);">{wallet.balanceSol.toFixed(4)}</p>
				</div>
				<div
					class="p-3"
					style="
						border-radius: var(--md-sys-shape-corner-small);
						background-color: var(--md-sys-color-surface-container-high);
					"
				>
					<p class="text-xs" style="color: var(--md-sys-color-on-surface-variant);">USDC</p>
					<p class="font-semibold" style="color: var(--md-sys-color-primary);">${wallet.balanceUsdc.toFixed(2)}</p>
				</div>
			</div>

			<!-- Disconnect — MD3 outlined button -->
			<button
				class="w-full h-10 text-sm font-medium transition-colors active:scale-95 touch-manipulation"
				style="
					border-radius: var(--md-sys-shape-corner-full);
					border: 1px solid var(--md-sys-color-error);
					color: var(--md-sys-color-error);
					background: transparent;
				"
				onclick={disconnectWallet}
			>
				Disconnect Wallet
			</button>
		{:else}
			<div class="text-center py-4">
				<div
					class="w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-3"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-8 h-8" style="color: var(--md-sys-color-on-surface-variant);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
						<rect x="2" y="6" width="20" height="12" rx="2" />
						<path d="M2 10h20" />
					</svg>
				</div>
				<p class="font-medium mb-1" style="color: var(--md-sys-color-on-surface);">
					No wallet connected
				</p>
				<p class="text-xs mb-4" style="color: var(--md-sys-color-on-surface-variant);">Connect to track your rewards on-chain</p>
				<WalletButton />
			</div>
		{/if}
	</div>

	<!-- Stats row — MD3 Cards -->
	<div class="grid grid-cols-3 gap-3 mb-6" style="animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1) 0.08s both;">
		<div
			class="p-4 text-center"
			style="
				border-radius: var(--md-sys-shape-corner-medium);
				background-color: var(--md-sys-color-surface-container-low);
			"
		>
			<p class="text-lg sm:text-xl font-semibold" style="color: #9945FF;">${stats.totalEarned.toFixed(0)}</p>
			<p class="text-[11px] sm:text-xs mt-0.5" style="color: var(--md-sys-color-on-surface-variant);">Total Earned</p>
		</div>
		<div
			class="p-4 text-center"
			style="
				border-radius: var(--md-sys-shape-corner-medium);
				background-color: var(--md-sys-color-surface-container-low);
			"
		>
			<p class="text-lg sm:text-xl font-semibold" style="color: #14F195;">{stats.dropsClaimed}</p>
			<p class="text-[11px] sm:text-xs mt-0.5" style="color: var(--md-sys-color-on-surface-variant);">Drops Claimed</p>
		</div>
		<div
			class="p-4 text-center"
			style="
				border-radius: var(--md-sys-shape-corner-medium);
				background-color: var(--md-sys-color-surface-container-low);
			"
		>
			<p class="text-lg sm:text-xl font-semibold" style="color: #FFB74D;">{stats.currentStreak}</p>
			<p class="text-[11px] sm:text-xs mt-0.5" style="color: var(--md-sys-color-on-surface-variant);">Day Streak</p>
		</div>
	</div>

	<!-- Settings — MD3 List -->
	<div
		class="overflow-hidden mb-6"
		style="
			animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1) 0.15s both;
			border-radius: var(--md-sys-shape-corner-medium);
			background-color: var(--md-sys-color-surface-container-low);
		"
	>
		<h3
			class="px-4 pt-4 pb-2 text-xs font-medium uppercase tracking-wider"
			style="color: var(--md-sys-color-on-surface-variant);"
		>
			Settings
		</h3>

		<!-- Dark mode toggle -->
		<button
			class="w-full flex items-center justify-between px-4 py-3.5 touch-manipulation"
			onclick={toggleDarkMode}
		>
			<div class="flex items-center gap-3">
				<div
					class="w-10 h-10 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-5 h-5" style="color: var(--md-sys-color-on-surface-variant);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						{#if darkMode}
							<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
						{:else}
							<circle cx="12" cy="12" r="5" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
						{/if}
					</svg>
				</div>
				<span class="text-sm font-medium" style="color: var(--md-sys-color-on-surface);">
					Dark Mode
				</span>
			</div>
			<!-- MD3 Switch -->
			<div
				class="shrink-0 rounded-full transition-colors duration-200 relative"
				style="
					width: 52px;
					height: 32px;
					background-color: {darkMode ? '#9945FF' : 'var(--md-sys-color-surface-container-highest)'};
					border: 2px solid {darkMode ? '#9945FF' : 'var(--md-sys-color-outline)'};
				"
			>
				<div
					class="absolute rounded-full transition-all duration-200"
					style="
						width: {darkMode ? '24px' : '16px'};
						height: {darkMode ? '24px' : '16px'};
						top: 50%;
						transform: translateY(-50%);
						left: {darkMode ? '22px' : '6px'};
						background-color: {darkMode ? 'var(--md-sys-color-on-primary)' : 'var(--md-sys-color-outline)'};
					"
				></div>
			</div>
		</button>

		<!-- Divider -->
		<div class="mx-4 h-px" style="background-color: var(--md-sys-color-surface-variant);"></div>

		<!-- Notifications toggle -->
		<button
			class="w-full flex items-center justify-between px-4 py-3.5 touch-manipulation"
			onclick={toggleNotifications}
		>
			<div class="flex items-center gap-3">
				<div
					class="w-10 h-10 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-5 h-5" style="color: var(--md-sys-color-on-surface-variant);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
						<path d="M13.73 21a2 2 0 0 1-3.46 0" />
					</svg>
				</div>
				<span class="text-sm font-medium" style="color: var(--md-sys-color-on-surface);">
					Notifications
				</span>
			</div>
			<!-- MD3 Switch -->
			<div
				class="shrink-0 rounded-full transition-colors duration-200 relative"
				style="
					width: 52px;
					height: 32px;
					background-color: {notifications ? '#9945FF' : 'var(--md-sys-color-surface-container-highest)'};
					border: 2px solid {notifications ? '#9945FF' : 'var(--md-sys-color-outline)'};
				"
			>
				<div
					class="absolute rounded-full transition-all duration-200"
					style="
						width: {notifications ? '24px' : '16px'};
						height: {notifications ? '24px' : '16px'};
						top: 50%;
						transform: translateY(-50%);
						left: {notifications ? '22px' : '6px'};
						background-color: {notifications ? 'var(--md-sys-color-on-primary)' : 'var(--md-sys-color-outline)'};
					"
				></div>
			</div>
		</button>
	</div>

	<!-- App info -->
	<div class="text-center pb-8" style="animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1) 0.2s both;">
		<p class="text-xs" style="color: var(--md-sys-color-on-surface-variant);">
			Member since {formatMemberSince(stats.memberSince)}
		</p>
		<p class="text-xs mt-1" style="color: var(--md-sys-color-outline);">
			{config.app.name} v{config.app.version} &middot; {config.app.poweredBy}
		</p>
	</div>
</div>
