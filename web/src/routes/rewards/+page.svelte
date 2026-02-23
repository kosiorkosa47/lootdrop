<script lang="ts">
	import { getClaimedRewards, getUserStats, getSettings, formatTokenAmount, formatTimeAgo } from '$lib/stores/app.svelte';

	const darkMode = $derived(getSettings().darkMode);
	const rewards = $derived(getClaimedRewards());
	const stats = $derived(getUserStats());

	function tokenColor(token: string): string {
		switch (token) {
			case 'SOL': return '#9945FF';
			case 'USDC': return '#2775CA';
			case 'BONK': return '#F5A623';
			default: return '#7C3AED';
		}
	}
</script>

<svelte:head>
	<title>LootDrop - Rewards</title>
</svelte:head>

<div class="px-4 pt-4">
	<!-- Header -->
	<div class="mb-6">
		<h1 class="text-2xl font-bold tracking-tight {darkMode ? 'text-white' : 'text-gray-900'}">
			My Rewards
		</h1>
		<p class="text-sm text-[#8B85A0] mt-0.5">
			Your claimed rewards history
		</p>
	</div>

	<!-- Total earned card -->
	<div
		class="rounded-2xl p-5 mb-6 relative overflow-hidden"
		style="background: linear-gradient(135deg, #7C3AED, #4F46E5); animation: slide-up 0.4s ease-out;"
	>
		<div class="absolute inset-0 opacity-15 pointer-events-none"
			style="background: linear-gradient(135deg, white 0%, transparent 40%);"
		></div>

		<div class="relative">
			<p class="text-sm text-white/70 mb-1">Total Earned</p>
			<p class="text-4xl font-bold text-white mb-3">
				${stats.totalEarned.toFixed(2)}
			</p>
			<div class="flex items-center gap-4">
				<div class="flex items-center gap-1.5">
					<div class="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center">
						<svg class="w-3 h-3 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
							<polyline points="20 12 20 22 4 22 4 12" />
							<rect x="2" y="7" width="20" height="5" />
						</svg>
					</div>
					<span class="text-xs text-white/80">{stats.dropsClaimed} claimed</span>
				</div>
				<div class="flex items-center gap-1.5">
					<div class="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center">
						<svg class="w-3 h-3 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
							<path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
						</svg>
					</div>
					<span class="text-xs text-white/80">{stats.currentStreak} day streak</span>
				</div>
			</div>
		</div>
	</div>

	<!-- Rewards list -->
	<div class="space-y-3 pb-4">
		{#if rewards.length === 0}
			<div class="text-center py-16" style="animation: fade-in 0.4s ease-out;">
				<div class="text-5xl mb-4">&#x1F381;</div>
				<p class="font-semibold mb-1 {darkMode ? 'text-white' : 'text-gray-900'}">
					No rewards yet
				</p>
				<p class="text-sm text-[#8B85A0]">Scan QR codes at merchant locations to earn rewards</p>
			</div>
		{:else}
			{#each rewards as reward, index (reward.id)}
				<div
					class="rounded-2xl p-4 transition-all {darkMode ? 'bg-[#2A2640]' : 'bg-white shadow-lg'}"
					style="animation: slide-up 0.4s ease-out {index * 0.05}s both; border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.08)' : 'rgba(0,0,0,0.05)'};"
				>
					<div class="flex items-center gap-3.5">
						<div
							class="w-11 h-11 rounded-xl flex items-center justify-center text-xl shrink-0 {!darkMode ? 'bg-gray-100' : ''}"
							style={darkMode ? 'background: rgba(255,255,255,0.1)' : ''}
						>
							{reward.businessIcon}
						</div>

						<div class="flex-1 min-w-0">
							<div class="flex items-center justify-between gap-2">
								<h3 class="font-semibold text-sm truncate {darkMode ? 'text-white' : 'text-gray-900'}">
									{reward.businessName}
								</h3>
								<span
									class="text-sm font-bold shrink-0"
									style="color: {tokenColor(reward.token)};"
								>
									+{formatTokenAmount(reward.amount, reward.token)} {reward.token}
								</span>
							</div>
							<div class="flex items-center gap-3 mt-1">
								<span class="text-[11px] text-[#8B85A0]">
									{formatTimeAgo(reward.claimedAt)}
								</span>
								<span class="text-[11px] text-[#8B85A0] font-mono truncate">
									tx: {reward.txHash}
								</span>
							</div>
						</div>
					</div>
				</div>
			{/each}
		{/if}
	</div>
</div>
