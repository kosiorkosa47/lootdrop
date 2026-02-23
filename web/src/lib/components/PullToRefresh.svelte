<script lang="ts">
	import type { Snippet } from 'svelte';
	import { getSettings } from '$lib/stores/app.svelte';

	interface Props {
		onRefresh: () => Promise<void>;
		children: Snippet;
	}

	let { onRefresh, children }: Props = $props();

	const darkMode = $derived(getSettings().darkMode);

	let pullDistance = $state(0);
	let isRefreshing = $state(false);
	let startY = $state(0);
	let isPulling = $state(false);

	const THRESHOLD = 80;
	const MAX_PULL = 120;

	function handleTouchStart(e: TouchEvent): void {
		const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
		if (scrollTop <= 0) {
			startY = e.touches[0].clientY;
			isPulling = true;
		}
	}

	function handleTouchMove(e: TouchEvent): void {
		if (!isPulling || isRefreshing) return;

		const currentY = e.touches[0].clientY;
		const diff = currentY - startY;

		if (diff > 0) {
			pullDistance = Math.min(MAX_PULL, diff * 0.5);
		}
	}

	async function handleTouchEnd(): Promise<void> {
		if (!isPulling) return;
		isPulling = false;

		if (pullDistance >= THRESHOLD && !isRefreshing) {
			isRefreshing = true;
			pullDistance = 60;
			await onRefresh();
			isRefreshing = false;
		}

		pullDistance = 0;
	}
</script>

<div
	class="relative min-h-screen"
	ontouchstart={handleTouchStart}
	ontouchmove={handleTouchMove}
	ontouchend={handleTouchEnd}
	role="region"
	aria-label="Pull to refresh"
>
	<!-- Refresh indicator -->
	<div
		class="absolute left-0 right-0 flex items-center justify-center pointer-events-none overflow-hidden transition-opacity duration-200"
		style="top: -60px; height: 60px; transform: translateY({pullDistance}px); opacity: {pullDistance > 10 ? 1 : 0};"
	>
		<div
			class="w-8 h-8 rounded-full flex items-center justify-center {!darkMode ? 'bg-white shadow-lg' : ''}"
			style={darkMode ? 'background: #2A2640' : ''}
		>
			{#if isRefreshing}
				<div
					class="w-5 h-5 border-2 rounded-full"
					style="border-color: rgba(124, 58, 237, 0.3); border-top-color: #7C3AED; animation: spin 0.6s linear infinite;"
				></div>
			{:else}
				<svg
					class="w-5 h-5 text-[#7C3AED] transition-transform duration-200"
					style="transform: rotate({pullDistance >= THRESHOLD ? 180 : pullDistance * 2}deg);"
					viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
				>
					<polyline points="6 9 12 15 18 9" />
				</svg>
			{/if}
		</div>
	</div>

	<!-- Content area with pull transform -->
	<div
		class="transition-transform"
		style="transform: translateY({pullDistance}px); transition-duration: {isPulling ? '0ms' : '300ms'};"
	>
		{@render children()}
	</div>
</div>
