/**
 * ==============================================================================
 * Student Task Manager - Frontend Controller (Vanilla JavaScript ES6+)
 * Communicates with Spring Boot REST APIs using the Fetch API.
 * ==============================================================================
 */

// API Configuration
const API_BASE_URL = '/api/tasks';

// Application State
const state = {
    tasks: [],
    stats: null,
    viewMode: 'cards', // 'cards' or 'table'
    filters: {
        keyword: '',
        status: '',
        priority: '',
        sortDir: 'asc',
        urgentOnly: false
    },
    editingTaskId: null,
    searchDebounceTimer: null
};

// Bootstrap Modal & Toast Instances
let taskModal = null;
let taskDetailsModal = null;
let deleteConfirmModal = null;
let liveToast = null;

// =============================================================================
// INITIALIZATION
// =============================================================================
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Bootstrap components
    taskModal = new bootstrap.Modal(document.getElementById('taskModal'));
    taskDetailsModal = new bootstrap.Modal(document.getElementById('taskDetailsModal'));
    deleteConfirmModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    liveToast = new bootstrap.Toast(document.getElementById('liveToast'), { delay: 3500 });

    // Display current date nicely in dashboard
    displayCurrentDate();

    // Initial data fetch from Spring Boot backend
    refreshAll();
});

function displayCurrentDate() {
    const options = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
    const dateStr = new Date().toLocaleDateString(undefined, options);
    const dateEl = document.getElementById('currentDateDisplay');
    if (dateEl) {
        dateEl.textContent = `Today: ${dateStr}`;
    }
}

// =============================================================================
// API INTEGRATION (FETCH API)
// =============================================================================

/**
 * Fetch all tasks from backend with current filters and sorting.
 */
async function loadTasks() {
    showLoading(true);

    try {
        const queryParams = new URLSearchParams();
        if (state.filters.keyword) queryParams.append('keyword', state.filters.keyword);
        if (state.filters.status) queryParams.append('status', state.filters.status);
        if (state.filters.priority) queryParams.append('priority', state.filters.priority);
        queryParams.append('sortBy', 'deadline');
        queryParams.append('sortDir', state.filters.sortDir);

        const response = await fetch(`${API_BASE_URL}?${queryParams.toString()}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        let tasks = await response.json();

        // If urgent filter is activated, filter client-side for tasks due in <= 3 days
        if (state.filters.urgentOnly) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const inThreeDays = new Date();
            inThreeDays.setDate(today.getDate() + 3);
            inThreeDays.setHours(23, 59, 59, 999);

            tasks = tasks.filter(t => {
                if (t.status === 'COMPLETED' || !t.deadline) return false;
                const d = new Date(t.deadline);
                return d <= inThreeDays;
            });
        }

        state.tasks = tasks;
        renderTasks();
        setBackendStatus(true);
    } catch (error) {
        console.error('Failed to load tasks:', error);
        setBackendStatus(false);
        showToast('Error', 'Could not load tasks from Spring Boot server.', 'danger');
    } finally {
        showLoading(false);
    }
}

/**
 * Fetch dashboard statistics from GET /api/tasks/stats
 */
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const stats = await response.json();
        state.stats = stats;
        renderDashboardStats(stats);
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}

/**
 * Convenience method to reload both dashboard stats and tasks list.
 */
async function refreshAll() {
    await Promise.all([loadDashboardStats(), loadTasks()]);
}

/**
 * Create a new task (POST /api/tasks)
 */
async function apiCreateTask(taskData) {
    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(taskData)
    });
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Failed to create task (Status ${response.status})`);
    }
    return await response.json();
}

/**
 * Update an existing task (PUT /api/tasks/{id})
 */
async function apiUpdateTask(id, taskData) {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(taskData)
    });
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Failed to update task (Status ${response.status})`);
    }
    return await response.json();
}

/**
 * Update task status (PATCH /api/tasks/{id}/status)
 */
async function apiUpdateStatus(id, newStatus) {
    const response = await fetch(`${API_BASE_URL}/${id}/status?status=${newStatus}`, {
        method: 'PATCH'
    });
    if (!response.ok) {
        throw new Error(`Failed to update status (Status ${response.status})`);
    }
    return await response.json();
}

/**
 * Delete a task (DELETE /api/tasks/{id})
 */
async function apiDeleteTask(id) {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE'
    });
    if (!response.ok && response.status !== 204) {
        throw new Error(`Failed to delete task (Status ${response.status})`);
    }
}

// =============================================================================
// DOM RENDERING
// =============================================================================

function renderDashboardStats(stats) {
    document.getElementById('statTotal').textContent = stats.totalTasks ?? 0;
    document.getElementById('statCompleted').textContent = stats.completedTasks ?? 0;
    document.getElementById('statPending').textContent = stats.pendingTasks ?? 0;
    document.getElementById('statInProgress').textContent = stats.inProgressTasks ?? 0;

    // Upcoming Deadlines Alert Strip
    const urgentStrip = document.getElementById('urgentAlertStrip');
    const urgentText = document.getElementById('urgentAlertText');
    const totalUrgent = (stats.upcomingDeadlinesCount || 0) + (stats.overdueCount || 0);

    if (totalUrgent > 0) {
        urgentStrip.classList.remove('d-none');
        urgentStrip.classList.add('d-flex');

        let msg = '';
        if (stats.overdueCount > 0 && stats.upcomingDeadlinesCount > 0) {
            msg = `You have ${stats.overdueCount} overdue task(s) and ${stats.upcomingDeadlinesCount} task(s) due within 3 days!`;
        } else if (stats.overdueCount > 0) {
            msg = `You have ${stats.overdueCount} overdue task(s) that need immediate attention!`;
        } else {
            msg = `You have ${stats.upcomingDeadlinesCount} task(s) due within the next 3 days!`;
        }
        urgentText.textContent = msg;
    } else {
        urgentStrip.classList.add('d-none');
        urgentStrip.classList.remove('d-flex');
    }
}

function renderTasks() {
    const cardsContainer = document.getElementById('tasksCardsContainer');
    const tableContainer = document.getElementById('tasksTableContainer');
    const tableBody = document.getElementById('tasksTableBody');
    const emptyState = document.getElementById('emptyState');

    if (!state.tasks || state.tasks.length === 0) {
        emptyState.classList.remove('d-none');
        cardsContainer.classList.add('d-none');
        tableContainer.classList.add('d-none');
        return;
    }

    emptyState.classList.add('d-none');

    if (state.viewMode === 'cards') {
        tableContainer.classList.add('d-none');
        cardsContainer.classList.remove('d-none');
        cardsContainer.innerHTML = state.tasks.map(task => renderTaskCard(task)).join('');
    } else {
        cardsContainer.classList.add('d-none');
        tableContainer.classList.remove('d-none');
        tableBody.innerHTML = state.tasks.map((task, idx) => renderTaskTableRow(task, idx + 1)).join('');
    }
}

function renderTaskCard(task) {
    const deadlineInfo = formatDeadline(task.deadline);
    const isCompleted = task.status === 'COMPLETED';

    return `
        <div class="col-12 col-md-6 col-lg-4">
            <div class="card task-item-card p-3 ${isCompleted ? 'completed-card' : ''}" id="task-card-${task.id}">
                <!-- Top Header: Subject & Priority -->
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="task-subject-badge" title="${escapeHtml(task.subject || 'General')}">
                        <i class="bi bi-book-half me-1"></i>${escapeHtml(task.subject || 'General')}
                    </span>
                    <span class="badge badge-priority-${task.priority}">
                        ${task.priority} PRIORITY
                    </span>
                </div>

                <!-- Title -->
                <h5 class="task-title fw-bold mb-2 text-dark text-truncate" title="${escapeHtml(task.title)}">
                    ${escapeHtml(task.title)}
                </h5>

                <!-- Description Snippet -->
                <p class="text-muted small mb-3 flex-grow-1" style="min-height: 38px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                    ${task.description ? escapeHtml(task.description) : '<em>No additional description.</em>'}
                </p>

                <!-- Deadline Badge -->
                <div class="mb-3 d-flex align-items-center justify-content-between">
                    <div class="deadline-text ${deadlineInfo.cssClass}">
                        <i class="bi bi-calendar-event"></i>
                        <span>${deadlineInfo.text}</span>
                    </div>
                </div>

                <!-- Footer: Status Dropdown & Action Buttons -->
                <div class="d-flex align-items-center justify-content-between pt-2 border-top">
                    <!-- Status selector -->
                    <select class="form-select form-select-sm status-select-sm badge-status-${task.status}" 
                            onchange="handleStatusChange(${task.id}, this.value)">
                        <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                        <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>Completed</option>
                    </select>

                    <!-- Action buttons -->
                    <div class="d-flex gap-1 ms-2">
                        <button class="btn btn-sm btn-outline-secondary btn-action-icon" onclick="openDetailsModal(${task.id})" title="View Details">
                            <i class="bi bi-eye"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-primary btn-action-icon" onclick="openEditModal(${task.id})" title="Edit Task">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger btn-action-icon" onclick="openDeleteModal(${task.id})" title="Delete Task">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function renderTaskTableRow(task, index) {
    const deadlineInfo = formatDeadline(task.deadline);
    const isCompleted = task.status === 'COMPLETED';

    return `
        <tr class="${isCompleted ? 'table-light text-muted' : ''}">
            <td class="text-muted small">${index}</td>
            <td>
                <div class="fw-semibold text-dark ${isCompleted ? 'text-decoration-line-through' : ''}">${escapeHtml(task.title)}</div>
                <small class="text-muted d-block text-truncate" style="max-width: 260px;">${escapeHtml(task.description || '')}</small>
            </td>
            <td><span class="badge bg-light text-dark border">${escapeHtml(task.subject || 'General')}</span></td>
            <td><span class="badge badge-priority-${task.priority}">${task.priority}</span></td>
            <td>
                <select class="form-select form-select-sm status-select-sm badge-status-${task.status}" 
                        onchange="handleStatusChange(${task.id}, this.value)">
                    <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                    <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                    <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>Completed</option>
                </select>
            </td>
            <td>
                <span class="deadline-text ${deadlineInfo.cssClass}">
                    <i class="bi bi-calendar-event me-1"></i>${deadlineInfo.text}
                </span>
            </td>
            <td class="text-end">
                <div class="d-inline-flex gap-1">
                    <button class="btn btn-sm btn-outline-secondary btn-action-icon" onclick="openDetailsModal(${task.id})" title="View Details">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-primary btn-action-icon" onclick="openEditModal(${task.id})" title="Edit Task">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger btn-action-icon" onclick="openDeleteModal(${task.id})" title="Delete Task">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `;
}

// =============================================================================
// MODAL CONTROLS & SUBMIT HANDLERS
// =============================================================================

function openCreateModal() {
    state.editingTaskId = null;
    document.getElementById('taskModalTitle').innerHTML = '<i class="bi bi-plus-circle text-primary me-2"></i>Add New Task';
    document.getElementById('saveTaskSubmitBtn').innerHTML = '<i class="bi bi-check2 me-1"></i> Save Task';
    document.getElementById('taskForm').reset();
    document.getElementById('taskIdInput').value = '';
    document.getElementById('taskPriorityInput').value = 'MEDIUM';
    document.getElementById('taskStatusInput').value = 'PENDING';

    // Set default deadline to today + 3 days
    const defaultDeadline = new Date();
    defaultDeadline.setDate(defaultDeadline.getDate() + 3);
    document.getElementById('taskDeadlineInput').value = defaultDeadline.toISOString().split('T')[0];
}

function openEditModal(taskId) {
    const task = state.tasks.find(t => t.id === taskId);
    if (!task) return;

    state.editingTaskId = taskId;
    document.getElementById('taskModalTitle').innerHTML = '<i class="bi bi-pencil-square text-primary me-2"></i>Edit Task';
    document.getElementById('saveTaskSubmitBtn').innerHTML = '<i class="bi bi-check2 me-1"></i> Update Task';

    document.getElementById('taskIdInput').value = task.id;
    document.getElementById('taskTitleInput').value = task.title;
    document.getElementById('taskSubjectInput').value = task.subject || '';
    document.getElementById('taskPriorityInput').value = task.priority;
    document.getElementById('taskStatusInput').value = task.status;
    document.getElementById('taskDeadlineInput').value = task.deadline || '';
    document.getElementById('taskDescriptionInput').value = task.description || '';

    taskModal.show();
}

async function handleTaskFormSubmit(event) {
    event.preventDefault();

    const title = document.getElementById('taskTitleInput').value.trim();
    const subject = document.getElementById('taskSubjectInput').value.trim();
    const priority = document.getElementById('taskPriorityInput').value;
    const status = document.getElementById('taskStatusInput').value;
    const deadline = document.getElementById('taskDeadlineInput').value || null;
    const description = document.getElementById('taskDescriptionInput').value.trim();

    if (!title || !subject) {
        showToast('Validation Error', 'Title and Subject are required.', 'warning');
        return;
    }

    const payload = { title, subject, priority, status, deadline, description };

    const submitBtn = document.getElementById('saveTaskSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

    try {
        if (state.editingTaskId) {
            await apiUpdateTask(state.editingTaskId, payload);
            showToast('Updated', 'Task successfully updated in MySQL database.', 'success');
        } else {
            await apiCreateTask(payload);
            showToast('Created', 'New task saved to MySQL database.', 'success');
        }

        taskModal.hide();
        await refreshAll();
    } catch (error) {
        console.error('Error saving task:', error);
        showToast('Error', error.message || 'Failed to save task.', 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-check2 me-1"></i> Save Task';
    }
}

function openDetailsModal(taskId) {
    const task = state.tasks.find(t => t.id === taskId);
    if (!task) return;

    const body = document.getElementById('taskDetailsBody');
    const deadlineInfo = formatDeadline(task.deadline);

    body.innerHTML = `
        <div class="mb-3">
            <span class="badge badge-priority-${task.priority} me-1">${task.priority} PRIORITY</span>
            <span class="badge badge-status-${task.status}">${task.status.replace('_', ' ')}</span>
        </div>
        <h4 class="fw-bold mb-2 text-dark">${escapeHtml(task.title)}</h4>
        <div class="text-muted small mb-3">
            <i class="bi bi-book me-1"></i> Course / Subject: <strong>${escapeHtml(task.subject || 'Not specified')}</strong>
        </div>
        <div class="p-3 bg-light rounded-3 mb-3">
            <label class="form-label text-muted small fw-bold mb-1">DESCRIPTION</label>
            <p class="mb-0 text-dark small" style="white-space: pre-wrap;">${task.description ? escapeHtml(task.description) : '<em>No description provided.</em>'}</p>
        </div>
        <div class="row g-2 small text-muted">
            <div class="col-6">
                <strong><i class="bi bi-calendar-event me-1"></i> Due Date:</strong><br>
                <span class="${deadlineInfo.cssClass}">${task.deadline || 'No deadline set'}</span>
            </div>
            <div class="col-6">
                <strong><i class="bi bi-clock-history me-1"></i> Created At:</strong><br>
                ${formatTimestamp(task.createdAt)}
            </div>
            <div class="col-12 mt-2">
                <span class="text-secondary">Task ID in MySQL: #<strong>${task.id}</strong></span>
            </div>
        </div>
    `;

    document.getElementById('detailsEditBtn').onclick = () => {
        taskDetailsModal.hide();
        openEditModal(task.id);
    };

    document.getElementById('detailsDeleteBtn').onclick = () => {
        taskDetailsModal.hide();
        openDeleteModal(task.id);
    };

    taskDetailsModal.show();
}

function openDeleteModal(taskId) {
    document.getElementById('taskToDeleteId').value = taskId;
    deleteConfirmModal.show();
}

async function confirmDeleteTask() {
    const id = document.getElementById('taskToDeleteId').value;
    if (!id) return;

    const btn = document.getElementById('confirmDeleteBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Deleting...';

    try {
        await apiDeleteTask(id);
        deleteConfirmModal.hide();
        showToast('Deleted', `Task #${id} removed from database.`, 'success');
        await refreshAll();
    } catch (error) {
        console.error('Delete error:', error);
        showToast('Error', 'Failed to delete task from database.', 'danger');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Delete Task';
    }
}

async function handleStatusChange(taskId, newStatus) {
    try {
        await apiUpdateStatus(taskId, newStatus);
        showToast('Status Updated', `Task status changed to ${newStatus.replace('_', ' ')}.`, 'info');
        await refreshAll();
    } catch (error) {
        console.error('Status change error:', error);
        showToast('Error', 'Failed to update task status.', 'danger');
        await loadTasks(); // Revert UI
    }
}

// =============================================================================
// SEARCH & FILTER HANDLERS
// =============================================================================

function handleSearchInput() {
    const val = document.getElementById('searchInput').value;
    document.getElementById('clearSearchBtn').style.display = val ? 'block' : 'none';

    clearTimeout(state.searchDebounceTimer);
    state.searchDebounceTimer = setTimeout(() => {
        state.filters.keyword = val.trim();
        loadTasks();
        renderActiveFilterPills();
    }, 300);
}

function clearSearch() {
    document.getElementById('searchInput').value = '';
    document.getElementById('clearSearchBtn').style.display = 'none';
    state.filters.keyword = '';
    loadTasks();
    renderActiveFilterPills();
}

function applyFilters() {
    state.filters.status = document.getElementById('statusFilter').value;
    state.filters.priority = document.getElementById('priorityFilter').value;
    state.filters.sortDir = document.getElementById('sortOrderSelect').value;
    state.filters.urgentOnly = false; // Reset urgent-only toggle on manual filter change

    renderActiveFilterPills();
    loadTasks();
}

function filterUpcomingUrgent() {
    state.filters.urgentOnly = true;
    renderActiveFilterPills();
    loadTasks();
    // Scroll smoothly to tasks
    document.getElementById('tasksSection').scrollIntoView({ behavior: 'smooth' });
}

function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('clearSearchBtn').style.display = 'none';
    document.getElementById('statusFilter').value = '';
    document.getElementById('priorityFilter').value = '';
    document.getElementById('sortOrderSelect').value = 'asc';

    state.filters.keyword = '';
    state.filters.status = '';
    state.filters.priority = '';
    state.filters.sortDir = 'asc';
    state.filters.urgentOnly = false;

    renderActiveFilterPills();
    loadTasks();
}

function renderActiveFilterPills() {
    const container = document.getElementById('activeFilterPills');
    const pillsContainer = document.getElementById('filterPillsContainer');
    pillsContainer.innerHTML = '';

    const activePills = [];

    if (state.filters.keyword) {
        activePills.push({ label: `Search: "${state.filters.keyword}"`, clearFn: () => clearSearch() });
    }
    if (state.filters.status) {
        activePills.push({ label: `Status: ${state.filters.status}`, clearFn: () => {
            document.getElementById('statusFilter').value = '';
            applyFilters();
        }});
    }
    if (state.filters.priority) {
        activePills.push({ label: `Priority: ${state.filters.priority}`, clearFn: () => {
            document.getElementById('priorityFilter').value = '';
            applyFilters();
        }});
    }
    if (state.filters.urgentOnly) {
        activePills.push({ label: `Filter: Urgent / Due Soon`, clearFn: () => {
            state.filters.urgentOnly = false;
            renderActiveFilterPills();
            loadTasks();
        }});
    }

    if (activePills.length > 0) {
        container.style.removeProperty('display');
        activePills.forEach(p => {
            const pill = document.createElement('span');
            pill.className = 'badge bg-primary-subtle text-primary border border-primary-subtle py-1 px-2 d-inline-flex align-items-center gap-1';
            pill.innerHTML = `${p.label} <i class="bi bi-x-circle-fill cursor-pointer"></i>`;
            pill.querySelector('i').onclick = p.clearFn;
            pillsContainer.appendChild(pill);
        });
    } else {
        container.style.setProperty('display', 'none', 'important');
    }
}

function setViewMode(mode) {
    state.viewMode = mode;
    document.getElementById('viewCardsBtn').classList.toggle('active', mode === 'cards');
    document.getElementById('viewTableBtn').classList.toggle('active', mode === 'table');
    renderTasks();
}

// =============================================================================
// HELPER FUNCTIONS & FORMATTERS
// =============================================================================

function formatDeadline(deadlineStr) {
    if (!deadlineStr) {
        return { text: 'No Deadline', cssClass: 'deadline-normal' };
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const deadline = new Date(deadlineStr);
    deadline.setHours(0, 0, 0, 0);

    const diffDays = Math.round((deadline - today) / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
        const absDays = Math.abs(diffDays);
        return {
            text: `Overdue by ${absDays} day${absDays > 1 ? 's' : ''} (${deadlineStr})`,
            cssClass: 'deadline-overdue'
        };
    } else if (diffDays === 0) {
        return { text: 'Due Today!', cssClass: 'deadline-soon' };
    } else if (diffDays === 1) {
        return { text: 'Due Tomorrow', cssClass: 'deadline-soon' };
    } else if (diffDays <= 3) {
        return { text: `Due in ${diffDays} days (${deadlineStr})`, cssClass: 'deadline-soon' };
    } else {
        return { text: `Due: ${deadlineStr}`, cssClass: 'deadline-normal' };
    }
}

function formatTimestamp(isoStr) {
    if (!isoStr) return 'N/A';
    try {
        const d = new Date(isoStr);
        return d.toLocaleString(undefined, {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch {
        return isoStr;
    }
}

function showLoading(isLoading) {
    const loadingState = document.getElementById('loadingState');
    if (isLoading) {
        loadingState.classList.remove('d-none');
    } else {
        loadingState.classList.add('d-none');
    }
}

function setBackendStatus(isConnected) {
    const badge = document.getElementById('backendStatusBadge');
    const alertBanner = document.getElementById('connectionAlert');

    if (isConnected) {
        badge.className = 'badge rounded-pill bg-light text-success border border-success-subtle d-flex align-items-center gap-1 py-2 px-3';
        badge.innerHTML = '<span class="status-dot bg-success"></span> Connected (Port 8080)';
        alertBanner.classList.add('d-none');
    } else {
        badge.className = 'badge rounded-pill bg-light text-danger border border-danger-subtle d-flex align-items-center gap-1 py-2 px-3';
        badge.innerHTML = '<span class="status-dot bg-danger"></span> Backend Offline';
        alertBanner.classList.remove('d-none');
    }
}

function showToast(title, message, variant = 'primary') {
    const toastEl = document.getElementById('liveToast');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');

    toastEl.className = `toast align-items-center text-white border-0 bg-${variant}`;
    toastMessage.textContent = `${title}: ${message}`;

    if (variant === 'success') {
        toastIcon.className = 'bi bi-check-circle-fill';
    } else if (variant === 'danger') {
        toastIcon.className = 'bi bi-exclamation-triangle-fill';
    } else if (variant === 'warning') {
        toastIcon.className = 'bi bi-exclamation-circle-fill';
    } else {
        toastIcon.className = 'bi bi-info-circle-fill';
    }

    liveToast.show();
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
