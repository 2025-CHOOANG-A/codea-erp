const sidebar = document.getElementById('sidebar');
const sidebarToggle = document.getElementById('sidebarToggle');
const sidebarClose = document.getElementById('sidebarClose');
sidebarToggle.addEventListener('click', () => sidebar.classList.add('show'));
sidebarClose.addEventListener('click', () => sidebar.classList.remove('show'));
document.addEventListener('click', (e) => {
	if (sidebar.classList.contains('show') && !sidebar.contains(e.target) && !sidebarToggle.contains(e.target)) {
		sidebar.classList.remove('show');
	}
});
