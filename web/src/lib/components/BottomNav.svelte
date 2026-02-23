<script lang="ts">
	import { page } from '$app/state';

	interface Tab {
		id: string;
		label: string;
		icon: string;
		href: string;
	}

	const tabs: Tab[] = [
		{ id: 'map', label: 'Map', icon: 'map', href: '/' },
		{ id: 'scan', label: 'Scan', icon: 'scan', href: '/scan' },
		{ id: 'rewards', label: 'Rewards', icon: 'gift', href: '/rewards' },
		{ id: 'profile', label: 'Profile', icon: 'user', href: '/profile' }
	];

	function isActive(href: string): boolean {
		const path = page.url.pathname;
		if (href === '/') return path === '/';
		return path.startsWith(href);
	}
</script>

<!-- MD3 Navigation Bar -->
<nav
	class="fixed bottom-0 left-0 right-0 z-40"
	style="
		padding-bottom: var(--sab);
		background-color: var(--md-sys-color-surface-container);
	"
>
	<div class="flex items-center justify-around mx-auto max-w-lg" style="height: 80px;">
		{#each tabs as tab (tab.id)}
			{@const active = isActive(tab.href)}
			<a
				href={tab.href}
				class="flex flex-col items-center justify-center gap-1 w-16 touch-manipulation no-underline"
				aria-label={tab.label}
				data-sveltekit-preload-data="tap"
			>
				<!-- Icon container with pill indicator -->
				<div class="relative flex items-center justify-center w-16 h-8">
					<!-- Active pill indicator -->
					{#if active}
						<div
							class="absolute inset-x-2 inset-y-0 rounded-full"
							style="background-color: var(--md-sys-color-secondary-container);"
						></div>
					{/if}

					<!-- Icon -->
					<div class="relative" style="color: {active ? 'var(--md-sys-color-on-secondary-container)' : 'var(--md-sys-color-on-surface-variant)'};">
						{#if tab.icon === 'map'}
							<svg class="w-6 h-6" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke="currentColor" stroke-width={active ? 0 : 2} stroke-linecap="round" stroke-linejoin="round">
								{#if active}
									<path d="M1 6v16l7-4 8 4 7-4V2l-7 4-8-4-7 4z" />
								{:else}
									<polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6" />
									<line x1="8" y1="2" x2="8" y2="18" />
									<line x1="16" y1="6" x2="16" y2="22" />
								{/if}
							</svg>
						{:else if tab.icon === 'scan'}
							<svg class="w-6 h-6" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke={active ? 'none' : 'currentColor'} stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
								{#if active}
									<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
									<circle cx="12" cy="13" r="4" fill="var(--md-sys-color-secondary-container)" />
								{:else}
									<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
									<circle cx="12" cy="13" r="4" />
								{/if}
							</svg>
						{:else if tab.icon === 'gift'}
							<svg class="w-6 h-6" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke={active ? 'none' : 'currentColor'} stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
								{#if active}
									<path d="M20 12v10H4V12" /><rect x="2" y="7" width="20" height="5" />
									<line x1="12" y1="22" x2="12" y2="7" stroke="var(--md-sys-color-secondary-container)" stroke-width="2" />
									<path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
									<path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
								{:else}
									<polyline points="20 12 20 22 4 22 4 12" />
									<rect x="2" y="7" width="20" height="5" />
									<line x1="12" y1="22" x2="12" y2="7" />
									<path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
									<path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
								{/if}
							</svg>
						{:else if tab.icon === 'user'}
							<svg class="w-6 h-6" viewBox="0 0 24 24" fill={active ? 'currentColor' : 'none'} stroke={active ? 'none' : 'currentColor'} stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
								{#if active}
									<circle cx="12" cy="7" r="4" />
									<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
								{:else}
									<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
									<circle cx="12" cy="7" r="4" />
								{/if}
							</svg>
						{/if}
					</div>
				</div>

				<!-- Label -->
				<span
					class="text-xs font-medium leading-none"
					style="color: {active ? 'var(--md-sys-color-on-surface)' : 'var(--md-sys-color-on-surface-variant)'};"
				>
					{tab.label}
				</span>
			</a>
		{/each}
	</div>
</nav>
