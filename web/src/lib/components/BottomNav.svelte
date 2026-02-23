<script lang="ts">
	import { page } from '$app/state';
	import { getSettings } from '$lib/stores/app.svelte';

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

	const darkMode = $derived(getSettings().darkMode);

	function isActive(href: string): boolean {
		const path = page.url.pathname;
		if (href === '/') return path === '/';
		return path.startsWith(href);
	}
</script>

<nav
	class="fixed bottom-0 left-0 right-0 z-40 border-t backdrop-blur-xl {darkMode ? 'border-white/10' : 'border-gray-200'}"
	style="padding-bottom: var(--sab); background: {darkMode ? 'rgba(30, 27, 46, 0.95)' : 'rgba(255, 255, 255, 0.95)'};"
>
	<div class="flex items-center justify-around h-16 max-w-lg mx-auto px-2">
		{#each tabs as tab (tab.id)}
			<a
				href={tab.href}
				class="flex flex-col items-center justify-center gap-0.5 w-16 h-14 rounded-xl transition-all duration-200 touch-manipulation {isActive(tab.href) ? 'text-[#7C3AED] scale-110' : darkMode ? 'text-[#8B85A0]' : 'text-[#6B6580]'}"
				aria-label={tab.label}
				data-sveltekit-preload-data="tap"
			>
				{#if tab.icon === 'map'}
					<svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
						<polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6" />
						<line x1="8" y1="2" x2="8" y2="18" />
						<line x1="16" y1="6" x2="16" y2="22" />
					</svg>
				{:else if tab.icon === 'scan'}
					<svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
						<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
						<circle cx="12" cy="13" r="4" />
					</svg>
				{:else if tab.icon === 'gift'}
					<svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
						<polyline points="20 12 20 22 4 22 4 12" />
						<rect x="2" y="7" width="20" height="5" />
						<line x1="12" y1="22" x2="12" y2="7" />
						<path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
						<path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
					</svg>
				{:else if tab.icon === 'user'}
					<svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
						<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
						<circle cx="12" cy="7" r="4" />
					</svg>
				{/if}

				<span class="text-[10px] font-medium leading-none">{tab.label}</span>

				{#if isActive(tab.href)}
					<div class="absolute -top-1 w-1 h-1 rounded-full bg-[#7C3AED]"></div>
				{/if}
			</a>
		{/each}
	</div>
</nav>
