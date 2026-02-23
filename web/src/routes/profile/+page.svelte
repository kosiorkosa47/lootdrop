<script lang="ts">
	import WalletButton from '$lib/components/WalletButton.svelte';
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
	<!-- Header -->
	<div class="mb-6">
		<h1 class="text-2xl font-bold tracking-tight {darkMode ? 'text-white' : 'text-gray-900'}">
			Profile
		</h1>
		<p class="text-sm text-[#8B85A0] mt-0.5">
			Wallet & settings
		</p>
	</div>

	<!-- Wallet section -->
	<div
		class="rounded-2xl p-5 mb-6 relative overflow-hidden {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
		style="border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.12)' : 'rgba(0,0,0,0.06)'}; animation: slide-up 0.4s ease-out;"
	>
		<div class="absolute inset-0 opacity-5 rounded-2xl pointer-events-none"
			style="background: linear-gradient(135deg, white 0%, transparent 50%);"
		></div>

		<div class="relative">
			{#if wallet.connected && wallet.address}
				<div class="flex items-center gap-3 mb-4">
					<div
						class="w-12 h-12 rounded-full flex items-center justify-center"
						style="background: linear-gradient(135deg, #7C3AED, #4F46E5);"
					>
						<svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
							<rect x="2" y="6" width="20" height="12" rx="2" />
							<path d="M2 10h20" />
						</svg>
					</div>
					<div class="flex-1">
						<p class="text-xs text-[#8B85A0]">Connected Wallet</p>
						<p class="font-mono text-sm font-semibold {darkMode ? 'text-white' : 'text-gray-900'}">
							{truncateAddress(wallet.address)}
						</p>
					</div>
					<div class="w-3 h-3 rounded-full bg-[#10B981]" title="Connected"></div>
				</div>

				<div class="grid grid-cols-2 gap-3 mb-4">
					<div
						class="rounded-xl p-3 {!darkMode ? 'bg-gray-50' : ''}"
						style={darkMode ? 'background: rgba(255,255,255,0.05)' : ''}
					>
						<p class="text-[11px] text-[#8B85A0]">SOL</p>
						<p class="font-bold" style="color: #9945FF;">{wallet.balanceSol.toFixed(4)}</p>
					</div>
					<div
						class="rounded-xl p-3 {!darkMode ? 'bg-gray-50' : ''}"
						style={darkMode ? 'background: rgba(255,255,255,0.05)' : ''}
					>
						<p class="text-[11px] text-[#8B85A0]">USDC</p>
						<p class="font-bold" style="color: #2775CA;">${wallet.balanceUsdc.toFixed(2)}</p>
					</div>
				</div>

				<button
					class="w-full h-11 rounded-xl text-sm font-medium transition-all active:scale-95 touch-manipulation {darkMode ? 'text-red-400' : 'bg-red-50 text-red-600'}"
					style={darkMode ? 'background: rgba(255,255,255,0.08)' : ''}
					onclick={disconnectWallet}
				>
					Disconnect Wallet
				</button>
			{:else}
				<div class="text-center py-4">
					<div
						class="w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-3 {!darkMode ? 'bg-gray-100' : ''}"
						style={darkMode ? 'background: rgba(255,255,255,0.08)' : ''}
					>
						<svg class="w-8 h-8 text-[#8B85A0]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
							<rect x="2" y="6" width="20" height="12" rx="2" />
							<path d="M2 10h20" />
						</svg>
					</div>
					<p class="font-semibold mb-1 {darkMode ? 'text-white' : 'text-gray-900'}">
						No wallet connected
					</p>
					<p class="text-xs text-[#8B85A0] mb-4">Connect to track your rewards on-chain</p>
					<WalletButton />
				</div>
			{/if}
		</div>
	</div>

	<!-- Stats grid -->
	<div class="grid grid-cols-3 gap-3 mb-6" style="animation: slide-up 0.4s ease-out 0.1s both;">
		<div
			class="rounded-2xl p-4 text-center {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
			style="border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.08)' : 'rgba(0,0,0,0.05)'};"
		>
			<p class="text-xl font-bold" style="color: #7C3AED;">${stats.totalEarned.toFixed(0)}</p>
			<p class="text-[11px] text-[#8B85A0] mt-0.5">Total Earned</p>
		</div>
		<div
			class="rounded-2xl p-4 text-center {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
			style="border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.08)' : 'rgba(0,0,0,0.05)'};"
		>
			<p class="text-xl font-bold" style="color: #10B981;">{stats.dropsClaimed}</p>
			<p class="text-[11px] text-[#8B85A0] mt-0.5">Drops Claimed</p>
		</div>
		<div
			class="rounded-2xl p-4 text-center {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
			style="border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.08)' : 'rgba(0,0,0,0.05)'};"
		>
			<p class="text-xl font-bold" style="color: #F59E0B;">{stats.currentStreak}</p>
			<p class="text-[11px] text-[#8B85A0] mt-0.5">Day Streak</p>
		</div>
	</div>

	<!-- Settings -->
	<div
		class="rounded-2xl overflow-hidden mb-6 {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
		style="border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.08)' : 'rgba(0,0,0,0.05)'}; animation: slide-up 0.4s ease-out 0.2s both;"
	>
		<h3 class="px-4 pt-4 pb-2 text-xs font-semibold uppercase tracking-wider text-[#8B85A0]">
			Settings
		</h3>

		<!-- Dark mode toggle -->
		<button
			class="w-full flex items-center justify-between px-4 py-3.5 transition-colors touch-manipulation"
			onclick={toggleDarkMode}
		>
			<div class="flex items-center gap-3">
				<div
					class="w-9 h-9 rounded-lg flex items-center justify-center {!darkMode ? 'bg-gray-100' : ''}"
					style={darkMode ? 'background: rgba(255,255,255,0.08)' : ''}
				>
					<svg class="w-5 h-5 {darkMode ? 'text-yellow-400' : 'text-gray-600'}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						{#if darkMode}
							<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
						{:else}
							<circle cx="12" cy="12" r="5" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
						{/if}
					</svg>
				</div>
				<span class="text-sm font-medium {darkMode ? 'text-white' : 'text-gray-900'}">
					Dark Mode
				</span>
			</div>
			<div
				class="w-11 h-6 rounded-full transition-colors relative {darkMode ? 'bg-[#7C3AED]' : 'bg-gray-300'}"
			>
				<div
					class="absolute top-0.5 w-5 h-5 rounded-full bg-white shadow-md transition-transform"
					style="transform: translateX({darkMode ? '22px' : '2px'});"
				></div>
			</div>
		</button>

		<!-- Divider -->
		<div
			class="mx-4 h-px {!darkMode ? 'bg-gray-100' : ''}"
			style={darkMode ? 'background: rgba(255,255,255,0.05)' : ''}
		></div>

		<!-- Notifications toggle -->
		<button
			class="w-full flex items-center justify-between px-4 py-3.5 transition-colors touch-manipulation"
			onclick={toggleNotifications}
		>
			<div class="flex items-center gap-3">
				<div
					class="w-9 h-9 rounded-lg flex items-center justify-center {!darkMode ? 'bg-gray-100' : ''}"
					style={darkMode ? 'background: rgba(255,255,255,0.08)' : ''}
				>
					<svg class="w-5 h-5 text-[#8B85A0]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
						<path d="M13.73 21a2 2 0 0 1-3.46 0" />
					</svg>
				</div>
				<span class="text-sm font-medium {darkMode ? 'text-white' : 'text-gray-900'}">
					Notifications
				</span>
			</div>
			<div
				class="w-11 h-6 rounded-full transition-colors relative {notifications ? 'bg-[#7C3AED]' : darkMode ? 'bg-gray-600' : 'bg-gray-300'}"
			>
				<div
					class="absolute top-0.5 w-5 h-5 rounded-full bg-white shadow-md transition-transform"
					style="transform: translateX({notifications ? '22px' : '2px'});"
				></div>
			</div>
		</button>
	</div>

	<!-- App info -->
	<div class="text-center pb-8" style="animation: fade-in 0.4s ease-out 0.3s both;">
		<p class="text-xs text-[#8B85A0]">
			Member since {formatMemberSince(stats.memberSince)}
		</p>
		<p class="text-xs mt-1" style="color: rgba(139, 133, 160, 0.6);">
			LootDrop v0.1.0 &middot; Powered by Solana
		</p>
	</div>
</div>
