<script lang="ts">
	import { getClaimedRewards, getUserStats, formatTokenAmount, formatTimeAgo } from '$lib/stores/app.svelte';

	const rewards = $derived(getClaimedRewards());
	const stats = $derived(getUserStats());

	function tokenColor(token: string): string {
		switch (token) {
			case 'SOL': return 'var(--md-sys-color-tertiary)';
			case 'USDC': return 'var(--md-sys-color-primary)';
			case 'BONK': return '#F5A623';
			default: return 'var(--md-sys-color-primary)';
		}
	}
</script>

<svelte:head>
	<title>LootDrop - Rewards</title>
</svelte:head>

<div class="px-4 pt-4">
	<!-- MD3 Top App Bar (Large) -->
	<div class="mb-6">
		<p class="text-sm font-medium mb-1" style="color: var(--md-sys-color-on-surface-variant);">
			Your claimed rewards history
		</p>
		<h1 class="text-3xl font-normal tracking-tight" style="color: var(--md-sys-color-on-surface);">
			My Rewards
		</h1>
	</div>

	<!-- Stats card — MD3 primary-container instead of gradient -->
	<div
		class="mb-6 p-5"
		style="
			animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1);
			background-color: var(--md-sys-color-primary-container);
			border-radius: var(--md-sys-shape-corner-large);
		"
	>
		<p class="text-sm mb-1" style="color: var(--md-sys-color-on-primary-container); opacity: 0.7;">
			Total Earned
		</p>
		<p class="text-3xl sm:text-4xl font-semibold mb-3" style="color: var(--md-sys-color-on-primary-container);">
			${stats.totalEarned.toFixed(2)}
		</p>
		<div class="flex items-center gap-4">
			<div class="flex items-center gap-1.5">
				<div
					class="w-6 h-6 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-primary); opacity: 0.3;"
				>
					<svg class="w-3.5 h-3.5" style="color: var(--md-sys-color-on-primary-container);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<polyline points="20 12 20 22 4 22 4 12" />
						<rect x="2" y="7" width="20" height="5" />
					</svg>
				</div>
				<span class="text-xs" style="color: var(--md-sys-color-on-primary-container);">{stats.dropsClaimed} claimed</span>
			</div>
			<div class="flex items-center gap-1.5">
				<div
					class="w-6 h-6 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-primary); opacity: 0.3;"
				>
					<svg class="w-3.5 h-3.5" style="color: var(--md-sys-color-on-primary-container);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
					</svg>
				</div>
				<span class="text-xs" style="color: var(--md-sys-color-on-primary-container);">{stats.currentStreak} day streak</span>
			</div>
		</div>
	</div>

	<!-- Rewards list — MD3 list items -->
	<div class="space-y-2 pb-4">
		{#if rewards.length === 0}
			<div class="text-center py-16" style="animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1);">
				<div
					class="w-16 h-16 mx-auto mb-4 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-8 h-8" style="color: var(--md-sys-color-on-surface-variant);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
						<polyline points="20 12 20 22 4 22 4 12" />
						<rect x="2" y="7" width="20" height="5" />
						<line x1="12" y1="22" x2="12" y2="7" />
						<path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
						<path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
					</svg>
				</div>
				<p class="font-medium mb-1" style="color: var(--md-sys-color-on-surface);">No rewards yet</p>
				<p class="text-sm" style="color: var(--md-sys-color-on-surface-variant);">Scan QR codes at merchant locations to earn rewards</p>
			</div>
		{:else}
			{#each rewards as reward, index (reward.id)}
				<div
					class="flex items-center gap-4 px-4 py-3 transition-colors"
					style="
						animation: slide-up 0.25s cubic-bezier(0.2, 0, 0, 1) {index * 0.04}s both;
						background-color: var(--md-sys-color-surface-container-low);
						border-radius: var(--md-sys-shape-corner-medium);
					"
				>
					<!-- Leading icon -->
					<div
						class="w-10 h-10 rounded-full flex items-center justify-center text-lg shrink-0"
						style="background-color: var(--md-sys-color-surface-container-high);"
					>
						{reward.businessIcon}
					</div>

					<!-- Headline + supporting text -->
					<div class="flex-1 min-w-0">
						<h3 class="text-sm font-medium truncate" style="color: var(--md-sys-color-on-surface);">
							{reward.businessName}
						</h3>
						<div class="flex items-center gap-2 mt-0.5">
							<span class="text-xs" style="color: var(--md-sys-color-on-surface-variant);">
								{formatTimeAgo(reward.claimedAt)}
							</span>
							<span class="text-xs font-mono truncate" style="color: var(--md-sys-color-outline);">
								tx: {reward.txHash}
							</span>
						</div>
					</div>

					<!-- Trailing text -->
					<span
						class="text-sm font-semibold shrink-0"
						style="color: {tokenColor(reward.token)};"
					>
						+{formatTokenAmount(reward.amount, reward.token)} {reward.token}
					</span>
				</div>
			{/each}
		{/if}
	</div>
</div>
