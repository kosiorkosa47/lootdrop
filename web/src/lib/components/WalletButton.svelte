<script lang="ts">
	import { getWallet, connectWallet, disconnectWallet, truncateAddress, getSettings } from '$lib/stores/app.svelte';

	let connecting = $state(false);

	const wallet = $derived(getWallet());
	const darkMode = $derived(getSettings().darkMode);

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
	<button
		class="flex items-center gap-2 h-10 px-4 rounded-xl text-sm font-medium transition-all active:scale-95 touch-manipulation {darkMode ? 'text-[#A78BFA]' : 'text-[#7C3AED]'}"
		style="background: {darkMode ? 'rgba(124, 58, 237, 0.15)' : 'rgba(124, 58, 237, 0.1)'};"
		onclick={handleDisconnect}
		title="Tap to disconnect"
	>
		<div class="w-2 h-2 rounded-full bg-[#10B981]"></div>
		{truncateAddress(wallet.address)}
	</button>
{:else}
	<button
		class="flex items-center gap-2 h-10 px-4 rounded-xl text-sm font-semibold text-white transition-all active:scale-95 touch-manipulation"
		style="background: linear-gradient(135deg, #7C3AED, #4F46E5);"
		onclick={handleConnect}
		disabled={connecting}
	>
		{#if connecting}
			<div class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full" style="animation: spin 0.6s linear infinite;"></div>
		{:else}
			<svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
				<rect x="2" y="6" width="20" height="12" rx="2" />
				<path d="M2 10h20" />
			</svg>
		{/if}
		{connecting ? 'Connecting...' : 'Connect Wallet'}
	</button>
{/if}
