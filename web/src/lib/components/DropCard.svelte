<script lang="ts">
	import type { Drop } from '$lib/types';
	import { formatDistance, formatTokenAmount, formatExpiry, claimDrop } from '$lib/stores/app.svelte';

	interface Props {
		drop: Drop;
		index: number;
	}

	let { drop, index }: Props = $props();

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
			case 'SOL': return 'var(--md-sys-color-tertiary)';
			case 'USDC': return 'var(--md-sys-color-primary)';
			case 'BONK': return '#F5A623';
			default: return 'var(--md-sys-color-primary)';
		}
	}
</script>

<!-- MD3 Filled Card -->
<button
	class="w-full text-left transition-all duration-200 active:scale-[0.98] touch-manipulation relative overflow-hidden"
	style="
		animation: slide-up 0.3s cubic-bezier(0.2, 0, 0, 1) {index * 0.05}s both;
		background-color: var(--md-sys-color-surface-container-low);
		border-radius: var(--md-sys-shape-corner-medium);
		padding: 16px;
		opacity: {(claimed || drop.claimed) ? 0.5 : 1};
	"
	onclick={handleTap}
	disabled={claiming}
>
	<div class="flex items-center gap-4 relative">
		<!-- Business icon -->
		<div
			class="w-12 h-12 rounded-full flex items-center justify-center text-2xl shrink-0"
			style="background-color: var(--md-sys-color-surface-container-high);"
		>
			{drop.businessIcon}
		</div>

		<!-- Content -->
		<div class="flex-1 min-w-0">
			<div class="flex items-center justify-between gap-2">
				<h3
					class="font-medium text-sm truncate"
					style="color: var(--md-sys-color-on-surface);"
				>
					{drop.businessName}
				</h3>
				<span
					class="text-sm font-semibold shrink-0 px-2.5 py-1 rounded-full"
					style="
						color: {tokenColor(drop.rewardToken)};
						background-color: var(--md-sys-color-surface-container-high);
					"
				>
					{formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken}
				</span>
			</div>

			<p class="text-xs mt-1 truncate" style="color: var(--md-sys-color-on-surface-variant);">
				{drop.description}
			</p>

			<div class="flex items-center gap-3 mt-2">
				<span class="text-xs flex items-center gap-1" style="color: var(--md-sys-color-outline);">
					<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="10" r="3" /><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 7 8 11.7z" />
					</svg>
					{formatDistance(drop.distanceMeters)}
				</span>
				<span class="text-xs flex items-center gap-1" style="color: var(--md-sys-color-outline);">
					<svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
					</svg>
					{formatExpiry(drop.expiresAt)}
				</span>
				{#if claimed || drop.claimed}
					<span class="text-xs font-medium" style="color: var(--color-success);">Claimed</span>
				{/if}
			</div>
		</div>

		<!-- Chevron -->
		{#if !claimed && !drop.claimed}
			<svg class="w-5 h-5 shrink-0" style="color: var(--md-sys-color-outline);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
				<polyline points="9 18 15 12 9 6" />
			</svg>
		{/if}
	</div>
</button>

<!-- MD3 Dialog — Confirm Claim -->
{#if showConfirm}
	<div class="fixed inset-0 z-50 flex items-center justify-center p-6" style="animation: fade-in 0.15s cubic-bezier(0.2, 0, 0, 1);">
		<!-- Scrim -->
		<button
			class="absolute inset-0"
			style="background-color: var(--md-sys-color-scrim); opacity: 0.32;"
			onclick={cancelClaim}
			aria-label="Cancel"
		></button>

		<!-- Dialog surface -->
		<div
			class="relative w-full max-w-sm"
			style="
				animation: slide-up 0.25s cubic-bezier(0.2, 0, 0, 1);
				background-color: var(--md-sys-color-surface-container-high);
				border-radius: var(--md-sys-shape-corner-extra-large);
				padding: 24px;
			"
		>
			<!-- Icon -->
			<div class="flex justify-center mb-4">
				<div
					class="w-14 h-14 rounded-full flex items-center justify-center text-3xl"
					style="background-color: var(--md-sys-color-primary-container);"
				>
					{drop.businessIcon}
				</div>
			</div>

			<!-- Headline -->
			<h3
				class="text-lg font-medium text-center mb-2"
				style="color: var(--md-sys-color-on-surface);"
			>
				Claim Reward?
			</h3>

			<!-- Supporting text -->
			<p class="text-sm text-center mb-6" style="color: var(--md-sys-color-on-surface-variant);">
				{drop.businessName} &mdash; {formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken}
			</p>

			<!-- Actions -->
			<div class="flex justify-end gap-2">
				<!-- Text button — Cancel -->
				<button
					class="h-10 px-6 rounded-full text-sm font-medium transition-colors active:scale-95"
					style="color: var(--md-sys-color-primary);"
					onclick={cancelClaim}
				>
					Cancel
				</button>

				<!-- Filled button — Confirm -->
				<button
					class="h-10 px-6 rounded-full text-sm font-medium transition-colors active:scale-95 flex items-center gap-2"
					style="
						background-color: var(--md-sys-color-primary);
						color: var(--md-sys-color-on-primary);
					"
					onclick={confirmClaim}
					disabled={claiming}
				>
					{#if claiming}
						<svg class="w-4 h-4" viewBox="0 0 24 24" style="animation: spin 0.8s linear infinite;">
							<circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="3" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
						</svg>
						Claiming...
					{:else}
						Confirm
					{/if}
				</button>
			</div>
		</div>
	</div>
{/if}

<!-- MD3 Snackbar — Success -->
{#if showSuccess}
	<div
		class="fixed bottom-24 left-4 right-4 z-50 flex items-center gap-3 max-w-lg mx-auto"
		style="
			animation: slide-up 0.25s cubic-bezier(0.2, 0, 0, 1);
			background-color: var(--md-sys-color-inverse-surface);
			color: var(--md-sys-color-inverse-on-surface);
			border-radius: var(--md-sys-shape-corner-small);
			padding: 14px 16px;
		"
	>
		<svg class="w-5 h-5 shrink-0" style="color: var(--color-success);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
			<polyline points="20 6 9 17 4 12" />
		</svg>
		<div class="flex-1 min-w-0">
			<p class="text-sm font-medium">Reward Claimed</p>
			<p class="text-xs opacity-80 truncate">
				{formatTokenAmount(drop.rewardAmount, drop.rewardToken)} {drop.rewardToken} from {drop.businessName}
			</p>
		</div>
	</div>
{/if}
