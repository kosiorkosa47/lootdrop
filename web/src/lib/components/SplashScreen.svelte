<script lang="ts">
	import { isSplashVisible, dismissSplash } from '$lib/stores/app.svelte';
	import { onMount } from 'svelte';

	let progress = $state(0);
	let fadeOut = $state(false);

	const visible = $derived(isSplashVisible());

	onMount(() => {
		const interval = setInterval(() => {
			progress += 2;
			if (progress >= 100) {
				clearInterval(interval);
				fadeOut = true;
				setTimeout(() => dismissSplash(), 400);
			}
		}, 40);

		return () => clearInterval(interval);
	});
</script>

{#if visible}
	<div
		class="fixed inset-0 z-50 flex flex-col items-center justify-center transition-opacity duration-300"
		class:opacity-0={fadeOut}
		style="background-color: var(--md-sys-color-surface);"
	>
		<!-- App icon -->
		<div
			class="w-24 h-24 rounded-3xl flex items-center justify-center mb-6"
			style="
				background-color: var(--md-sys-color-primary-container);
				animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1);
			"
		>
			<svg viewBox="0 0 80 80" class="w-14 h-14">
				<path
					d="M40 8 C40 8 16 40 16 56 C16 69.3 26.7 80 40 80 C53.3 80 64 69.3 64 56 C64 40 40 8 40 8Z"
					fill="var(--md-sys-color-on-primary-container)"
					opacity="0.9"
				/>
				<circle cx="40" cy="58" r="12" fill="var(--md-sys-color-primary-container)" />
				<text x="40" y="64" text-anchor="middle" fill="var(--md-sys-color-on-primary-container)" font-family="'Roboto', system-ui" font-weight="700" font-size="14">S</text>
			</svg>
		</div>

		<!-- App name -->
		<h1
			class="text-2xl font-medium mb-1 tracking-tight"
			style="color: var(--md-sys-color-on-surface); animation: fade-in 0.4s cubic-bezier(0.2, 0, 0, 1) 0.15s both;"
		>
			LootDrop
		</h1>
		<p
			class="text-sm mb-10"
			style="color: var(--md-sys-color-on-surface-variant); animation: fade-in 0.4s cubic-bezier(0.2, 0, 0, 1) 0.25s both;"
		>
			Walk in. Scan. Earn.
		</p>

		<!-- MD3 Circular progress indicator -->
		<div
			class="w-12 h-12"
			style="animation: fade-in 0.4s cubic-bezier(0.2, 0, 0, 1) 0.35s both;"
		>
			<svg viewBox="0 0 48 48" class="w-full h-full" style="animation: spin 1.4s linear infinite;">
				<circle
					cx="24"
					cy="24"
					r="20"
					fill="none"
					stroke="var(--md-sys-color-primary)"
					stroke-width="4"
					stroke-linecap="round"
					style="animation: md3-circular-progress 1.4s ease-in-out infinite;"
				/>
			</svg>
		</div>
	</div>
{/if}
