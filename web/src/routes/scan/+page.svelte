<script lang="ts">
	import { claimDrop } from '$lib/stores/app.svelte';
	import { MOCK_DROPS } from '$lib/data/mock';

	type ScanState = 'idle' | 'requesting' | 'scanning' | 'processing' | 'success' | 'error';

	let scanState = $state<ScanState>('idle');
	let scanResult = $state<{ businessName: string; amount: number; token: string } | null>(null);

	async function requestCamera(): Promise<void> {
		scanState = 'requesting';
		await new Promise((resolve) => setTimeout(resolve, 1000));
		scanState = 'scanning';
	}

	async function simulateScan(): Promise<void> {
		scanState = 'processing';
		await new Promise((resolve) => setTimeout(resolve, 2000));

		const available = MOCK_DROPS.filter((d) => !d.claimed);
		if (available.length > 0) {
			const drop = available[Math.floor(Math.random() * available.length)];
			claimDrop(drop.id);
			scanResult = {
				businessName: drop.businessName,
				amount: drop.rewardAmount,
				token: drop.rewardToken
			};
			scanState = 'success';
		} else {
			scanState = 'error';
		}
	}

	function resetScanner(): void {
		scanState = 'idle';
		scanResult = null;
	}
</script>

<svelte:head>
	<title>LootDrop - Scan</title>
</svelte:head>

<div class="px-4 pt-4 min-h-screen flex flex-col">
	<!-- MD3 Top App Bar -->
	<div class="mb-6">
		<p class="text-sm font-medium mb-1" style="color: var(--md-sys-color-on-surface-variant);">
			Scan at a merchant location to claim
		</p>
		<h1 class="text-3xl font-normal tracking-tight" style="color: var(--md-sys-color-on-surface);">
			Scan QR Code
		</h1>
	</div>

	<!-- Scanner area -->
	<div class="flex-1 flex flex-col items-center justify-center -mt-8">
		{#if scanState === 'idle'}
			<div class="text-center" style="animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1);">
				<!-- Viewfinder -->
				<div
					class="w-64 h-64 mx-auto flex items-center justify-center mb-8 relative"
					style="
						border: 2px dashed var(--md-sys-color-outline);
						border-radius: var(--md-sys-shape-corner-extra-large);
						background-color: var(--md-sys-color-surface-container-low);
					"
				>
					<div class="absolute top-3 left-3 w-8 h-8 border-t-3 border-l-3 rounded-tl-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute top-3 right-3 w-8 h-8 border-t-3 border-r-3 rounded-tr-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute bottom-3 left-3 w-8 h-8 border-b-3 border-l-3 rounded-bl-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute bottom-3 right-3 w-8 h-8 border-b-3 border-r-3 rounded-br-lg" style="border-color: var(--md-sys-color-primary);"></div>

					<svg class="w-16 h-16" style="color: var(--md-sys-color-outline);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
						<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
						<circle cx="12" cy="13" r="4" />
					</svg>
				</div>

				<p class="text-sm max-w-xs mx-auto leading-relaxed mb-6" style="color: var(--md-sys-color-on-surface-variant);">
					Point your camera at the QR code displayed at a merchant location to claim your reward.
				</p>

				<!-- MD3 Filled Button -->
				<button
					class="h-14 px-8 text-base font-medium transition-colors active:scale-95 touch-manipulation"
					style="
						min-width: 200px;
						border-radius: var(--md-sys-shape-corner-full);
						background-color: var(--md-sys-color-primary);
						color: var(--md-sys-color-on-primary);
					"
					onclick={requestCamera}
				>
					Start Scanning
				</button>
			</div>

		{:else if scanState === 'requesting'}
			<div class="text-center" style="animation: fade-in 0.2s cubic-bezier(0.2, 0, 0, 1);">
				<div
					class="w-16 h-16 mx-auto mb-6 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-8 h-8" viewBox="0 0 24 24" style="animation: spin 1.4s linear infinite;">
						<circle cx="12" cy="12" r="9" fill="none" stroke="var(--md-sys-color-primary)" stroke-width="3" stroke-linecap="round" style="animation: md3-circular-progress 1.4s ease-in-out infinite;" />
					</svg>
				</div>
				<p class="text-sm" style="color: var(--md-sys-color-on-surface-variant);">Requesting camera access...</p>
			</div>

		{:else if scanState === 'scanning'}
			<div class="text-center" style="animation: fade-in 0.2s cubic-bezier(0.2, 0, 0, 1);">
				<!-- Camera preview mock -->
				<div
					class="w-64 h-64 mx-auto flex items-center justify-center mb-8 relative overflow-hidden"
					style="
						border-radius: var(--md-sys-shape-corner-extra-large);
						background-color: var(--md-sys-color-surface-container-highest);
					"
				>
					<!-- Scan line -->
					<div
						class="absolute left-4 right-4 h-0.5"
						style="background-color: var(--md-sys-color-primary); animation: scan-line 2s ease-in-out infinite;"
					></div>

					<div class="absolute top-4 left-4 w-10 h-10 border-t-3 border-l-3 rounded-tl-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute top-4 right-4 w-10 h-10 border-t-3 border-r-3 rounded-tr-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute bottom-4 left-4 w-10 h-10 border-b-3 border-l-3 rounded-bl-lg" style="border-color: var(--md-sys-color-primary);"></div>
					<div class="absolute bottom-4 right-4 w-10 h-10 border-b-3 border-r-3 rounded-br-lg" style="border-color: var(--md-sys-color-primary);"></div>

					<p class="relative text-xs" style="color: var(--md-sys-color-on-surface-variant);">Camera Preview</p>
				</div>

				<p class="text-sm mb-6" style="color: var(--md-sys-color-on-surface-variant);">Align QR code within the frame</p>

				<!-- MD3 Filled Button -->
				<button
					class="h-14 px-8 text-base font-medium transition-colors active:scale-95 touch-manipulation"
					style="
						min-width: 200px;
						border-radius: var(--md-sys-shape-corner-full);
						background-color: var(--md-sys-color-primary);
						color: var(--md-sys-color-on-primary);
					"
					onclick={simulateScan}
				>
					Simulate Scan
				</button>
			</div>

		{:else if scanState === 'processing'}
			<div class="text-center" style="animation: fade-in 0.2s cubic-bezier(0.2, 0, 0, 1);">
				<div
					class="w-20 h-20 mx-auto mb-6 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-10 h-10" viewBox="0 0 24 24" style="animation: spin 1.4s linear infinite;">
						<circle cx="12" cy="12" r="9" fill="none" stroke="var(--md-sys-color-primary)" stroke-width="3" stroke-linecap="round" style="animation: md3-circular-progress 1.4s ease-in-out infinite;" />
					</svg>
				</div>
				<p class="font-medium" style="color: var(--md-sys-color-on-surface);">Verifying QR Code...</p>
				<p class="text-sm mt-1" style="color: var(--md-sys-color-on-surface-variant);">Submitting proof-of-visit to Solana</p>
			</div>

		{:else if scanState === 'success' && scanResult}
			<!-- MD3 Success state -->
			<div class="text-center" style="animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1);">
				<div
					class="w-20 h-20 mx-auto mb-4 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-primary-container);"
				>
					<svg class="w-10 h-10" style="color: var(--md-sys-color-on-primary-container);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
						<polyline points="20 6 9 17 4 12" />
					</svg>
				</div>
				<h2 class="text-xl font-medium mb-2" style="color: var(--md-sys-color-on-surface);">
					Reward Claimed!
				</h2>
				<p class="text-sm mb-2" style="color: var(--md-sys-color-on-surface-variant);">{scanResult.businessName}</p>
				<p class="text-3xl font-semibold mb-8" style="color: var(--md-sys-color-primary);">
					+{scanResult.amount} {scanResult.token}
				</p>

				<button
					class="h-14 px-8 text-base font-medium transition-colors active:scale-95 touch-manipulation"
					style="
						min-width: 200px;
						border-radius: var(--md-sys-shape-corner-full);
						background-color: var(--md-sys-color-primary);
						color: var(--md-sys-color-on-primary);
					"
					onclick={resetScanner}
				>
					Scan Another
				</button>
			</div>

		{:else}
			<!-- Error state -->
			<div class="text-center" style="animation: fade-in 0.2s cubic-bezier(0.2, 0, 0, 1);">
				<div
					class="w-20 h-20 mx-auto mb-4 rounded-full flex items-center justify-center"
					style="background-color: var(--md-sys-color-surface-container-high);"
				>
					<svg class="w-10 h-10" style="color: var(--md-sys-color-error);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10" /><line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" />
					</svg>
				</div>
				<h2 class="text-xl font-medium mb-2" style="color: var(--md-sys-color-on-surface);">
					No Available Drops
				</h2>
				<p class="text-sm mb-8" style="color: var(--md-sys-color-on-surface-variant);">All nearby rewards have been claimed.</p>
				<button
					class="h-14 px-8 text-base font-medium transition-colors active:scale-95 touch-manipulation"
					style="
						border-radius: var(--md-sys-shape-corner-full);
						background-color: var(--md-sys-color-primary);
						color: var(--md-sys-color-on-primary);
					"
					onclick={resetScanner}
				>
					Try Again
				</button>
			</div>
		{/if}
	</div>
</div>

<style>
	@keyframes scan-line {
		0%, 100% { top: 15%; }
		50% { top: 80%; }
	}
</style>
