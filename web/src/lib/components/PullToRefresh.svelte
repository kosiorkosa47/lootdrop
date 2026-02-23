<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		onRefresh: () => Promise<void>;
		children: Snippet;
	}

	let { onRefresh, children }: Props = $props();

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
	<!-- MD3 Refresh indicator -->
	<div
		class="absolute left-0 right-0 flex items-center justify-center pointer-events-none overflow-hidden transition-opacity duration-200"
		style="top: -60px; height: 60px; transform: translateY({pullDistance}px); opacity: {pullDistance > 10 ? 1 : 0};"
	>
		<div
			class="w-10 h-10 rounded-full flex items-center justify-center"
			style="
				background-color: var(--md-sys-color-surface-container-highest);
				box-shadow: 0 1px 3px rgba(0,0,0,0.2);
			"
		>
			{#if isRefreshing}
				<svg class="w-6 h-6" viewBox="0 0 24 24" style="animation: spin 1.4s linear infinite;">
					<circle
						cx="12" cy="12" r="9"
						fill="none"
						stroke="var(--md-sys-color-primary)"
						stroke-width="3"
						stroke-linecap="round"
						style="animation: md3-circular-progress 1.4s ease-in-out infinite;"
					/>
				</svg>
			{:else}
				<svg
					class="w-5 h-5 transition-transform duration-200"
					style="color: var(--md-sys-color-primary); transform: rotate({pullDistance >= THRESHOLD ? 180 : pullDistance * 2}deg);"
					viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
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
