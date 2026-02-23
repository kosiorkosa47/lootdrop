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
		class="fixed inset-0 z-50 flex flex-col items-center justify-center"
		class:opacity-0={fadeOut}
		class:transition-opacity={fadeOut}
		class:duration-400={fadeOut}
		style="background: linear-gradient(135deg, #0F0B1A 0%, #1a1040 50%, #0F0B1A 100%);"
	>
		<!-- Animated glow background -->
		<div
			class="absolute inset-0 opacity-30"
			style="background: radial-gradient(circle at 50% 40%, rgba(124, 58, 237, 0.4), transparent 60%);"
		></div>

		<!-- Logo -->
		<div class="relative mb-8" style="animation: bounce-in 0.6s ease-out;">
			<div
				class="w-28 h-28 rounded-3xl flex items-center justify-center"
				style="background: linear-gradient(135deg, #7C3AED, #4F46E5); animation: pulse-glow 2s ease-in-out infinite;"
			>
				<svg viewBox="0 0 80 80" class="w-16 h-16">
					<path
						d="M40 8 C40 8 16 40 16 56 C16 69.3 26.7 80 40 80 C53.3 80 64 69.3 64 56 C64 40 40 8 40 8Z"
						fill="white"
						opacity="0.95"
					/>
					<circle cx="40" cy="58" r="12" fill="#7C3AED" />
					<text x="40" y="64" text-anchor="middle" fill="white" font-family="system-ui" font-weight="700" font-size="14">S</text>
				</svg>
			</div>
		</div>

		<!-- App name -->
		<h1 class="text-3xl font-bold text-white mb-2 tracking-tight" style="animation: fade-in 0.8s ease-out 0.3s both;">
			LootDrop
		</h1>
		<p class="text-sm text-[#8B85A0] mb-12" style="animation: fade-in 0.8s ease-out 0.5s both;">
			Walk in. Scan. Earn.
		</p>

		<!-- Progress bar -->
		<div class="w-48 h-1 rounded-full bg-white/10 overflow-hidden" style="animation: fade-in 0.8s ease-out 0.7s both;">
			<div
				class="h-full rounded-full transition-all duration-100 ease-linear"
				style="width: {progress}%; background: linear-gradient(90deg, #7C3AED, #A78BFA);"
			></div>
		</div>

		<!-- Skeleton loading indicators -->
		<div class="absolute bottom-20 left-0 right-0 px-6 space-y-3" style="animation: fade-in 0.8s ease-out 0.9s both;">
			{#each [1, 2, 3] as _}
				<div class="h-16 rounded-xl bg-white/5 animate-pulse"></div>
			{/each}
		</div>
	</div>
{/if}
