<script lang="ts">
	import { getSettings, claimDrop } from '$lib/stores/app.svelte';
	import { MOCK_DROPS } from '$lib/data/mock';

	type ScanState = 'idle' | 'requesting' | 'scanning' | 'processing' | 'success' | 'error';

	const darkMode = $derived(getSettings().darkMode);

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
	<!-- Header -->
	<div class="mb-6">
		<h1 class="text-2xl font-bold tracking-tight {darkMode ? 'text-white' : 'text-gray-900'}">
			Scan QR Code
		</h1>
		<p class="text-sm text-[#8B85A0] mt-0.5">
			Scan at a merchant location to claim
		</p>
	</div>

	<!-- Scanner area -->
	<div class="flex-1 flex flex-col items-center justify-center -mt-8">
		{#if scanState === 'idle'}
			<div class="text-center" style="animation: fade-in 0.4s ease-out;">
				<div
					class="w-64 h-64 mx-auto rounded-3xl flex items-center justify-center mb-8 relative {!darkMode ? 'bg-gray-100' : ''}"
					style="border: 2px dashed rgba(124, 58, 237, 0.3); {darkMode ? 'background: rgba(255,255,255,0.05)' : ''}"
				>
					<div class="absolute top-3 left-3 w-8 h-8 border-t-3 border-l-3 border-[#7C3AED] rounded-tl-lg"></div>
					<div class="absolute top-3 right-3 w-8 h-8 border-t-3 border-r-3 border-[#7C3AED] rounded-tr-lg"></div>
					<div class="absolute bottom-3 left-3 w-8 h-8 border-b-3 border-l-3 border-[#7C3AED] rounded-bl-lg"></div>
					<div class="absolute bottom-3 right-3 w-8 h-8 border-b-3 border-r-3 border-[#7C3AED] rounded-br-lg"></div>

					<svg class="w-16 h-16 text-[#8B85A0]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
						<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
						<circle cx="12" cy="13" r="4" />
					</svg>
				</div>

				<p class="text-sm text-[#8B85A0] mb-6 max-w-xs mx-auto leading-relaxed">
					Point your camera at the QR code displayed at a merchant location to claim your reward.
				</p>

				<button
					class="h-14 px-8 rounded-2xl font-semibold text-white text-base transition-all active:scale-95 touch-manipulation"
					style="background: linear-gradient(135deg, #7C3AED, #4F46E5); min-width: 200px;"
					onclick={requestCamera}
				>
					Start Scanning
				</button>
			</div>

		{:else if scanState === 'requesting'}
			<div class="text-center" style="animation: fade-in 0.3s ease-out;">
				<div class="w-16 h-16 mx-auto mb-6 rounded-full flex items-center justify-center" style="background: rgba(124, 58, 237, 0.15);">
					<div class="w-8 h-8 border-2 rounded-full" style="border-color: rgba(124, 58, 237, 0.3); border-top-color: #7C3AED; animation: spin 0.6s linear infinite;"></div>
				</div>
				<p class="text-sm text-[#8B85A0]">Requesting camera access...</p>
			</div>

		{:else if scanState === 'scanning'}
			<div class="text-center" style="animation: fade-in 0.3s ease-out;">
				<div
					class="w-64 h-64 mx-auto rounded-3xl flex items-center justify-center mb-8 relative overflow-hidden {darkMode ? 'bg-gray-900' : 'bg-gray-200'}"
				>
					<div class="absolute inset-0 bg-gradient-to-b from-gray-800 to-gray-900 opacity-80"></div>

					<div
						class="absolute left-4 right-4 h-0.5 bg-[#7C3AED] shadow-lg"
						style="animation: scan-line 2s ease-in-out infinite; box-shadow: 0 0 8px #7C3AED;"
					></div>

					<div class="absolute top-4 left-4 w-10 h-10 border-t-3 border-l-3 border-[#7C3AED] rounded-tl-lg"></div>
					<div class="absolute top-4 right-4 w-10 h-10 border-t-3 border-r-3 border-[#7C3AED] rounded-tr-lg"></div>
					<div class="absolute bottom-4 left-4 w-10 h-10 border-b-3 border-l-3 border-[#7C3AED] rounded-bl-lg"></div>
					<div class="absolute bottom-4 right-4 w-10 h-10 border-b-3 border-r-3 border-[#7C3AED] rounded-br-lg"></div>

					<p class="relative text-xs text-white/60">Camera Preview</p>
				</div>

				<p class="text-sm text-[#8B85A0] mb-6">Align QR code within the frame</p>

				<button
					class="h-14 px-8 rounded-2xl font-semibold text-white text-base transition-all active:scale-95 touch-manipulation"
					style="background: linear-gradient(135deg, #7C3AED, #4F46E5); min-width: 200px;"
					onclick={simulateScan}
				>
					Simulate Scan
				</button>
			</div>

		{:else if scanState === 'processing'}
			<div class="text-center" style="animation: fade-in 0.3s ease-out;">
				<div
					class="w-24 h-24 mx-auto mb-6 rounded-full flex items-center justify-center"
					style="background: linear-gradient(135deg, rgba(124, 58, 237, 0.2), rgba(79, 70, 229, 0.1)); animation: pulse-glow 1.5s ease-in-out infinite;"
				>
					<div class="w-10 h-10 border-3 rounded-full" style="border-color: rgba(124, 58, 237, 0.3); border-top-color: #7C3AED; animation: spin 0.8s linear infinite;"></div>
				</div>
				<p class="font-semibold {darkMode ? 'text-white' : 'text-gray-900'}">Verifying QR Code...</p>
				<p class="text-sm text-[#8B85A0] mt-1">Submitting proof-of-visit to Solana</p>
			</div>

		{:else if scanState === 'success' && scanResult}
			<div class="text-center" style="animation: bounce-in 0.5s ease-out;">
				<div class="text-6xl mb-4">&#x1F389;</div>
				<h2 class="text-xl font-bold mb-2 {darkMode ? 'text-white' : 'text-gray-900'}">
					Reward Claimed!
				</h2>
				<p class="text-sm text-[#8B85A0] mb-2">{scanResult.businessName}</p>
				<p class="text-3xl font-bold mb-8" style="color: #7C3AED;">
					+{scanResult.amount} {scanResult.token}
				</p>

				<button
					class="h-14 px-8 rounded-2xl font-semibold text-white text-base transition-all active:scale-95 touch-manipulation"
					style="background: linear-gradient(135deg, #7C3AED, #4F46E5); min-width: 200px;"
					onclick={resetScanner}
				>
					Scan Another
				</button>
			</div>

		{:else}
			<div class="text-center" style="animation: fade-in 0.3s ease-out;">
				<div class="text-5xl mb-4">&#x274C;</div>
				<h2 class="text-xl font-bold mb-2 {darkMode ? 'text-white' : 'text-gray-900'}">
					No Available Drops
				</h2>
				<p class="text-sm text-[#8B85A0] mb-8">All nearby rewards have been claimed.</p>
				<button
					class="h-14 px-8 rounded-2xl font-semibold text-white text-base transition-all active:scale-95 touch-manipulation"
					style="background: linear-gradient(135deg, #7C3AED, #4F46E5);"
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
