import { useState } from 'react'

type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'DONE'

interface Task {
  id: string
  description: string
  status: TaskStatus
  budgetLimit: number
  budgetSpent: number
  createdAt: string
}

const STATUS_COLUMNS: { key: TaskStatus; label: string }[] = [
  { key: 'PENDING', label: '📋 Pending' },
  { key: 'IN_PROGRESS', label: '⚙️ In Progress' },
  { key: 'REVIEW', label: '🔍 Review' },
  { key: 'DONE', label: '✅ Done' },
]

const SAMPLE_TASKS: Task[] = [
  { id: '1', description: 'Implement email validation', status: 'PENDING', budgetLimit: 4000, budgetSpent: 0, createdAt: new Date().toISOString() },
  { id: '2', description: 'Add user registration endpoint', status: 'IN_PROGRESS', budgetLimit: 5000, budgetSpent: 1200, createdAt: new Date().toISOString() },
  { id: '3', description: 'Write unit tests for OrderService', status: 'REVIEW', budgetLimit: 3000, budgetSpent: 2800, createdAt: new Date().toISOString() },
  { id: '4', description: 'Refactor database connection pool', status: 'DONE', budgetLimit: 2000, budgetSpent: 1900, createdAt: new Date().toISOString() },
]

function TaskCard({ task }: { task: Task }) {
  const budgetPercent = Math.round((task.budgetSpent / task.budgetLimit) * 100)
  const budgetColor = budgetPercent > 80 ? '#ef4444' : budgetPercent > 50 ? '#f59e0b' : '#22c55e'

  return (
    <div className="task-card">
      <p className="task-desc">{task.description}</p>
      <div className="task-meta">
        <span className="task-id">#{task.id.slice(0, 6)}</span>
        <div className="budget-bar">
          <div className="budget-fill" style={{ width: `${Math.min(budgetPercent, 100)}%`, backgroundColor: budgetColor }} />
        </div>
        <span className="budget-text">{task.budgetSpent}/{task.budgetLimit} tokens</span>
      </div>
    </div>
  )
}

function NewTaskForm({ onAdd }: { onAdd: (desc: string, budget: number) => void }) {
  const [desc, setDesc] = useState('')
  const [budget, setBudget] = useState(4000)

  return (
    <form className="new-task-form" onSubmit={e => { e.preventDefault(); if (desc.trim()) { onAdd(desc.trim(), budget); setDesc('') } }}>
      <input
        type="text"
        placeholder="Describe the task..."
        value={desc}
        onChange={e => setDesc(e.target.value)}
        className="task-input"
      />
      <div className="budget-row">
        <label>Budget: {budget} tokens</label>
        <input
          type="range"
          min={500}
          max={20000}
          step={500}
          value={budget}
          onChange={e => setBudget(Number(e.target.value))}
        />
      </div>
      <button type="submit" className="btn-primary">Create Task</button>
    </form>
  )
}

export default function App() {
  const [tasks, setTasks] = useState<Task[]>(SAMPLE_TASKS)

  const handleAdd = (description: string, budgetLimit: number) => {
    const task: Task = {
      id: String(Date.now()),
      description,
      status: 'PENDING',
      budgetLimit,
      budgetSpent: 0,
      createdAt: new Date().toISOString(),
    }
    setTasks(prev => [task, ...prev])
  }

  return (
    <div className="app">
      <header className="header">
        <div className="header-top">
          <h1>
            <span className="logo">{'>'}_</span> Agentry
          </h1>
          <span className="subtitle">Multi-Agent Code Review System</span>
        </div>
        <NewTaskForm onAdd={handleAdd} />
      </header>

      <main className="board">
        {STATUS_COLUMNS.map(col => (
          <div key={col.key} className="column">
            <h2 className="column-title">{col.label}</h2>
            <div className="card-list">
              {tasks
                .filter(t => t.status === col.key)
                .map(t => <TaskCard key={t.id} task={t} />)}
              {tasks.filter(t => t.status === col.key).length === 0 && (
                <p className="empty-col">No tasks</p>
              )}
            </div>
          </div>
        ))}
      </main>

      <footer className="footer">
        <span>Agentry v0.1.0</span>
        <span>Made with ❤️</span>
      </footer>
    </div>
  )
}
