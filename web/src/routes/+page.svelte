<script lang="ts">
	import DropCard from '$lib/components/DropCard.svelte';
	import PullToRefresh from '$lib/components/PullToRefresh.svelte';
	import WalletButton from '$lib/components/WalletButton.svelte';
	import { getDrops, refreshDrops, getWallet } from '$lib/stores/app.svelte';

	const wallet = $derived(getWallet());
	const drops = $derived(getDrops());

	const sortedDrops = $derived(
		[...drops].sort((a, b) => {
			if (a.claimed !== b.claimed) return a.claimed ? 1 : -1;
			return a.distanceMeters - b.distanceMeters;
		})
	);

	const availableCount = $derived(drops.filter((d) => !d.claimed).length);
</script>

<svelte:head>
	<title>LootDrop - Nearby Drops</title>
</svelte:head>

<PullToRefresh onRefresh={refreshDrops}>
	<div class="px-4 pt-4">
		<!-- MD3 Top App Bar (Large) -->
		<div class="mb-2">
			<div class="flex items-center justify-between mb-1">
				<p class="text-sm font-medium" style="color: var(--md-sys-color-on-surface-variant);">
					{availableCount} reward{availableCount !== 1 ? 's' : ''} available
				</p>
				<WalletButton />
			</div>
			<h1
				class="text-3xl font-normal tracking-tight"
				style="color: var(--md-sys-color-on-surface);"
			>
				Nearby Drops
			</h1>
		</div>

		<!-- Balance card (shown when wallet connected) -->
		{#if wallet.connected}
			<div
				class="mb-4 mt-4 p-4"
				style="
					animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1);
					background: linear-gradient(135deg, #9945FF 0%, #14F195 100%);
					border-radius: var(--md-sys-shape-corner-medium);
				"
			>
				<div class="flex items-center justify-around">
					<div class="text-center">
						<p class="text-xs mb-0.5" style="color: rgba(255,255,255,0.75);">SOL Balance</p>
						<p class="text-lg font-semibold" style="color: #FFFFFF;">
							{wallet.balanceSol.toFixed(2)}
						</p>
					</div>
					<div class="w-px h-8" style="background-color: rgba(255,255,255,0.25);"></div>
					<div class="text-center">
						<p class="text-xs mb-0.5" style="color: rgba(255,255,255,0.75);">USDC Balance</p>
						<p class="text-lg font-semibold" style="color: #FFFFFF;">
							${wallet.balanceUsdc.toFixed(2)}
						</p>
					</div>
				</div>
			</div>
		{/if}

		<!-- MD3 Filter Chips -->
		<div class="flex gap-2 mb-4 mt-4 overflow-x-auto pb-1 -mx-4 px-4" style="-ms-overflow-style: none; scrollbar-width: none;">
			{#each ['All', 'Food', 'Fitness', 'Retail', 'Fun', 'Services'] as category, i}
				<button
					class="shrink-0 h-8 px-4 text-sm font-medium transition-colors active:scale-95 touch-manipulation"
					style="
						border-radius: var(--md-sys-shape-corner-small);
						{i === 0
							? `background-color: var(--md-sys-color-secondary-container); color: var(--md-sys-color-on-secondary-container);`
							: `background-color: transparent; color: var(--md-sys-color-on-surface-variant); border: 1px solid var(--md-sys-color-outline);`
						}
					"
				>
					{category}
				</button>
			{/each}
		</div>

		<!-- Drop cards list -->
		<div class="space-y-3 pb-4">
			{#each sortedDrops as drop, index (drop.id)}
				<DropCard {drop} {index} />
			{/each}
		</div>
	</div>
</PullToRefresh>
