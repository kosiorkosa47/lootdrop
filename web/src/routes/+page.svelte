<script lang="ts">
	import DropCard from '$lib/components/DropCard.svelte';
	import PullToRefresh from '$lib/components/PullToRefresh.svelte';
	import WalletButton from '$lib/components/WalletButton.svelte';
	import { getDrops, refreshDrops, getWallet, getSettings } from '$lib/stores/app.svelte';

	const darkMode = $derived(getSettings().darkMode);
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
		<!-- Header -->
		<div class="flex items-center justify-between mb-6">
			<div>
				<h1 class="text-2xl font-bold tracking-tight {darkMode ? 'text-white' : 'text-gray-900'}">
					Nearby Drops
				</h1>
				<p class="text-sm text-[#8B85A0] mt-0.5">
					{availableCount} reward{availableCount !== 1 ? 's' : ''} available
				</p>
			</div>
			<WalletButton />
		</div>

		<!-- Balance bar (shown when wallet connected) -->
		{#if wallet.connected}
			<div
				class="rounded-2xl p-4 mb-5 relative overflow-hidden"
				style="background: linear-gradient(135deg, rgba(124, 58, 237, 0.15), rgba(79, 70, 229, 0.08)); border: 1px solid rgba(124, 58, 237, 0.15); animation: slide-up 0.4s ease-out;"
			>
				<div class="absolute inset-0 opacity-10 pointer-events-none"
					style="background: linear-gradient(135deg, white 0%, transparent 40%);"
				></div>

				<div class="flex items-center justify-around relative">
					<div class="text-center">
						<p class="text-xs text-[#8B85A0] mb-0.5">SOL Balance</p>
						<p class="text-lg font-bold" style="color: #9945FF;">{wallet.balanceSol.toFixed(2)}</p>
					</div>
					<div class="w-px h-8 bg-white/10"></div>
					<div class="text-center">
						<p class="text-xs text-[#8B85A0] mb-0.5">USDC Balance</p>
						<p class="text-lg font-bold" style="color: #2775CA;">${wallet.balanceUsdc.toFixed(2)}</p>
					</div>
				</div>
			</div>
		{/if}

		<!-- Category filter pills -->
		<div class="flex gap-2 mb-4 overflow-x-auto pb-1 -mx-4 px-4 scrollbar-hide">
			{#each ['All', 'Food', 'Fitness', 'Retail', 'Fun', 'Services'] as category, i}
				<button
					class="shrink-0 h-8 px-4 rounded-full text-xs font-medium transition-all active:scale-95 touch-manipulation {i === 0 ? 'bg-[#7C3AED] text-white' : darkMode ? 'text-[#8B85A0]' : 'bg-gray-100 text-gray-600'}"
					style={i !== 0 && darkMode ? 'background: rgba(255,255,255,0.08)' : ''}
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
