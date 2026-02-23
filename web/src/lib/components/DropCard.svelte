<script lang="ts">
	import type { Drop } from '$lib/types';
	import { formatDistance, formatTokenAmount, formatExpiry, claimDrop, getSettings } from '$lib/stores/app.svelte';

	interface Props {
		drop: Drop;
		index: number;
	}

	let { drop, index }: Props = $props();

	const darkMode = $derived(getSettings().darkMode);

	let claiming = $state(false);
	let claimed = $state(false);
	let showConfirm = $state(false);
	let showSuccess = $state(false);

	function handleTap(): void {
		if (drop.claimed || claimed) return;
		showConfirm = true;
	}

	async function confirmClaim(): Promise<void> {
		claiming = true;
		await new Promise((resolve) => setTimeout(resolve, 1500));
		const result = claimDrop(drop.id);
		claiming = false;
		showConfirm = false;

		if (result) {
			claimed = true;
			showSuccess = true;
			setTimeout(() => { showSuccess = false; }, 3000);
		}
	}

	function cancelClaim(): void {
		showConfirm = false;
	}

	function tokenColor(token: string): string {
		switch (token) {
			case 'SOL': return '#9945FF';
			case 'USDC': return '#2775CA';
			case 'BONK': return '#F5A623';
			default: return '#7C3AED';
		}
	}
</script>

<!-- Drop Card -->
<button
	class="w-full text-left rounded-2xl p-4 transition-all duration-200 active:scale-[0.98] touch-manipulation relative overflow-hidden {(claimed || drop.claimed) ? 'opacity-60' : ''} {!darkMode && !claimed ? 'bg-white shadow-lg' : ''} {!darkMode && claimed ? 'bg-gray-100' : ''}"
	style="animation: slide-up 0.4s ease-out {index * 0.06}s both; border: 1px solid {darkMode ? 'rgba(124, 58, 237, 0.12)' : 'rgba(0,0,0,0.06)'}; {darkMode ? (claimed ? 'background: rgba(42, 38, 64, 0.5)' : 'background: #2A2640') : ''}"
	onclick={handleTap}
	disabled={claiming}
>
	<!-- Glassmorphism highlight -->
	<div class="absolute inset-0 opacity-5 rounded-2xl pointer-events-none"
		style="background: linear-gradient(135deg, white 0%, transparent 50%);"
	></div>

	<div class="flex items-center gap-3.5 relative">
		<!-- Business icon -->
		<div
			class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl shrink-0 {!darkMode ? 'bg-gray-100' : ''}"
			style={darkMode ? 'background: rgba(255,255,255,0.1)' : ''}
		>
			{drop.businessIcon}
		</div>

		<!-- Content -->
		<div class="flex-1 min-w-0">
			<div class="flex items-center justify-between gap-2">
				<h3 class="font-semibold text-sm truncate {darkMode ? 'text-white' : 'text-gray-900'}">
					{drop.businessName}
				</h3>
				<span
					class="text-sm font-bold shrink-0 px-2 py-0.5 rounded-lg"
					style="color: {tokenColor(drop.rewardToken)}; background: {tokenColor(drop.rewardToken)}15;"
				>
					{formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken}
				</span>
			</div>

			<p class="text-xs text-[#8B85A0] mt-0.5 truncate">{drop.description}</p>

			<div class="flex items-center gap-3 mt-1.5">
				<span class="text-[11px] text-[#8B85A0] flex items-center gap-1">
					<svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="10" r="3" /><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 7 8 11.7z" />
					</svg>
					{formatDistance(drop.distanceMeters)}
				</span>
				<span class="text-[11px] text-[#8B85A0] flex items-center gap-1">
					<svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
					</svg>
					{formatExpiry(drop.expiresAt)}
				</span>
				{#if claimed || drop.claimed}
					<span class="text-[11px] text-[#10B981] font-medium">Claimed</span>
				{/if}
			</div>
		</div>

		<!-- Chevron -->
		{#if !claimed && !drop.claimed}
			<svg class="w-4 h-4 text-[#8B85A0] shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
				<polyline points="9 18 15 12 9 6" />
			</svg>
		{/if}
	</div>
</button>

<!-- Confirm Dialog -->
{#if showConfirm}
	<div class="fixed inset-0 z-50 flex items-end justify-center p-4" style="animation: fade-in 0.2s ease-out;">
		<button class="absolute inset-0 bg-black/60 backdrop-blur-sm" onclick={cancelClaim} aria-label="Cancel"></button>

		<div
			class="relative w-full max-w-lg rounded-2xl p-6 mb-4 {darkMode ? 'bg-[#2A2640]' : 'bg-white'}"
			style="animation: slide-up 0.3s ease-out; border: 1px solid rgba(124, 58, 237, 0.15);"
		>
			<div class="text-center">
				<div class="text-4xl mb-3">{drop.businessIcon}</div>
				<h3 class="text-lg font-bold mb-1 {darkMode ? 'text-white' : 'text-gray-900'}">
					Claim Reward?
				</h3>
				<p class="text-sm text-[#8B85A0] mb-4">
					{drop.businessName} -- {formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken}
				</p>

				<div class="flex gap-3">
					<button
						class="flex-1 h-12 rounded-xl font-semibold text-sm transition-all active:scale-95 {darkMode ? 'text-white' : 'bg-gray-100 text-gray-700'}"
						style={darkMode ? 'background: rgba(255,255,255,0.1)' : ''}
						onclick={cancelClaim}
					>
						Cancel
					</button>
					<button
						class="flex-1 h-12 rounded-xl font-semibold text-sm text-white transition-all active:scale-95 flex items-center justify-center gap-2"
						style="background: linear-gradient(135deg, #7C3AED, #4F46E5);"
						onclick={confirmClaim}
						disabled={claiming}
					>
						{#if claiming}
							<div class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full" style="animation: spin 0.6s linear infinite;"></div>
							Claiming...
						{:else}
							Confirm
						{/if}
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}

<!-- Success Toast -->
{#if showSuccess}
	<div
		class="fixed top-16 left-4 right-4 z-50 flex items-center gap-3 rounded-xl p-4 shadow-xl max-w-lg mx-auto"
		style="background: linear-gradient(135deg, #065F46, #047857); animation: slide-up 0.3s ease-out;"
	>
		<div class="text-2xl" style="animation: bounce-in 0.5s ease-out;">&#x2705;</div>
		<div>
			<p class="text-sm font-semibold text-white">Reward Claimed!</p>
			<p class="text-xs text-white/70">{formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken} from {drop.businessName}</p>
		</div>
	</div>
{/if}
