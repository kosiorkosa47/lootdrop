<script lang="ts">
	import '../app.css';
	import type { Snippet } from 'svelte';
	import { onMount } from 'svelte';
	import SplashScreen from '$lib/components/SplashScreen.svelte';
	import BottomNav from '$lib/components/BottomNav.svelte';
	import { isSplashVisible } from '$lib/stores/app.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const showApp = $derived(!isSplashVisible());

	onMount(() => {
		// Register service worker
		if ('serviceWorker' in navigator) {
			navigator.serviceWorker.register('/sw.js').catch((err) => {
				if (import.meta.env.DEV) {
					console.warn('SW registration failed:', err);
				}
			});
		}
	});
</script>

<SplashScreen />

{#if showApp}
	<main
		class="min-h-screen pb-[calc(var(--tab-bar-height)+var(--sab))]"
		style="padding-top: var(--sat); animation: fade-in 0.4s ease-out;"
	>
		{@render children()}
	</main>

	<BottomNav />
{/if}
