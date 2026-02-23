<script lang="ts">
	import { getWallet, connectWallet, disconnectWallet, truncateAddress } from '$lib/stores/app.svelte';

	let connecting = $state(false);

	const wallet = $derived(getWallet());

	async function handleConnect(): Promise<void> {
		connecting = true;
		await new Promise((resolve) => setTimeout(resolve, 800));
		connectWallet();
		connecting = false;
	}

	function handleDisconnect(): void {
		disconnectWallet();
	}
</script>

{#if wallet.connected && wallet.address}
	<!-- MD3 Filled Tonal Button — Connected state -->
	<button
		class="flex items-center gap-2 px-4 text-sm font-medium transition-colors active:scale-95 touch-manipulation"
		style="
			height: 40px;
			border-radius: 20px;
			background-color: var(--md-sys-color-secondary-container);
			color: var(--md-sys-color-on-secondary-container);
		"
		onclick={handleDisconnect}
		title="Tap to disconnect"
	>
		<div class="w-2 h-2 rounded-full" style="background-color: #14F195;"></div>
		{truncateAddress(wallet.address)}
	</button>
{:else}
	<!-- MD3 Filled Tonal Button — Connect state -->
	<button
		class="flex items-center gap-2 px-5 text-sm font-medium transition-colors active:scale-95 touch-manipulation"
		style="
			height: 40px;
			border-radius: 20px;
			background-color: var(--md-sys-color-secondary-container);
			color: var(--md-sys-color-on-secondary-container);
		"
		onclick={handleConnect}
		disabled={connecting}
	>
		{#if connecting}
			<svg class="w-4 h-4" viewBox="0 0 24 24" style="animation: spin 0.8s linear infinite;">
				<circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="3" stroke-dasharray="15.7 15.7" stroke-linecap="round" />
			</svg>
		{:else}
			<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
				<rect x="2" y="6" width="20" height="12" rx="2" />
				<path d="M2 10h20" />
			</svg>
		{/if}
		{connecting ? 'Connecting...' : 'Connect Wallet'}
	</button>
{/if}
