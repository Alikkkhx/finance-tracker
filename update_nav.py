import os
import re

nav_html = """    <!-- Top Navigation -->
    <header class="top-nav">
        <div class="nav-container">
            <a href="/dashboard" class="nav-logo">
                <div class="logo-icon">💰</div>
                <h1>Smart Expense</h1>
            </a>
            <button class="menu-toggle" onclick="document.getElementById('mobileMenu').classList.toggle('show')">☰</button>
            <nav class="nav-menu" id="mobileMenu">
                <a href="/dashboard">📊 Dashboard</a>
                <a href="/expenses">💸 Expenses</a>
                <a href="/incomes">💵 Income</a>
                <a href="/analytics">📈 Analytics</a>
                <a href="/budget">🎯 Budget</a>
                <a href="/profile">👤 Profile</a>
            </nav>
            <div class="nav-user">
                <div class="avatar" th:text="${#strings.substring(session.fullName ?: 'U', 0, 1)}">U</div>
                <a href="/logout" class="btn btn-secondary btn-sm" style="margin-left:8px;">Sign Out</a>
            </div>
        </div>
    </header>
    <style>
    #mobileMenu { display: flex; }
    @media (max-width: 768px) {
        #mobileMenu { display: none; }
        #mobileMenu.show { display: flex; flex-direction: column; background: var(--bg-primary); position: absolute; top: 70px; left: 0; right: 0; padding: 20px; z-index: 1000; border-bottom: 1px solid var(--border-glass); box-shadow: 0 10px 20px rgba(0,0,0,0.5); }
    }
    </style>"""

directory = "src/main/resources/templates"
for filename in os.listdir(directory):
    if filename.endswith(".html") and filename not in ['login.html', 'register.html']:
        path = os.path.join(directory, filename)
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace sidebar
        content = re.sub(r'<!-- Sidebar -->\s*<nav class="sidebar">[\s\S]*?</nav>', nav_html, content)
        
        # In Dashboard, add Quick Actions
        if filename == 'dashboard.html':
            quick_actions = """        <div style="display:flex; gap:12px; margin-bottom:24px; flex-wrap:wrap;">
            <a href="/expenses" class="btn btn-danger">➕ Add Expense</a>
            <a href="/incomes" class="btn btn-success" style="background:var(--success);color:#fff;border:none;">💸 Add Income</a>
            <a href="/budget" class="btn btn-secondary">🎯 Set Budget</a>
        </div>
        <!-- Stats Cards -->"""
            content = content.replace("<!-- Stats Cards -->", quick_actions)
            
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)

print("HTML templates successfully updated to top-nav")
