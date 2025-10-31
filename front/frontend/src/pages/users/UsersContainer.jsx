// src/pages/users/UsersContainer.jsx
import { useEffect, useState } from 'react'
import { message } from 'antd'
import UsersTable from './UsersTable'
import UserEditModal from './UserEditModal'
import ChangePasswordModal from './ChangePasswordModal'
import { api } from '../../api/api.js'

export default function UsersContainer() {
    const [users, setUsers] = useState([])
    const [loading, setLoading] = useState(true)

    const [editingUser, setEditingUser] = useState(null)
    const [passwordUser, setPasswordUser] = useState(null)
    const [savingEdit, setSavingEdit] = useState(false)
    const [savingPass, setSavingPass] = useState(false)

    // =========================================================
    // 🔹 Загрузка списка пользователей
    // =========================================================
    useEffect(() => {
        const load = async () => {
            try {
                const data = await api.get('/users')
                setUsers(data || [])
            } catch (e) {
                console.error(e)
                message.error(e.message || 'Не удалось загрузить пользователей')
            } finally {
                setLoading(false)
            }
        }
        load()
    }, [])

    // =========================================================
    // 🔹 Открыть модалки
    // =========================================================
    const onEdit = (user) => {
        if (passwordUser) return
        setEditingUser(user)
    }

    const onChangePassword = (user) => {
        if (editingUser) return
        setPasswordUser(user)
    }

    // =========================================================
    // 🔹 Сохранение профиля (PUT /api/users/{username})
    // =========================================================
    const onSave = async (values) => {
        if (!editingUser?.username) return
        setSavingEdit(true)
        try {
            const updated = await api.put(
                `/users/${encodeURIComponent(editingUser.username)}`,
                {
                    fullName: values.fullName ?? null,
                    phone: values.phone ?? null,
                    email: values.email ?? null,
                    role: values.role ?? null,
                    isBlocked: values.isBlocked ?? null,
                }
            )

            setUsers((prev) =>
                prev.map((u) =>
                    u.username === updated.username ? { ...u, ...updated } : u
                )
            )

            message.success('Изменения сохранены')
            setEditingUser(null)
        } catch (e) {
            console.error(e)
            message.error(e.message || 'Ошибка при сохранении изменений')
        } finally {
            setSavingEdit(false)
        }
    }

    // =========================================================
    // 🔹 Смена пароля (PUT /api/users/password)
    // =========================================================
    const onSavePassword = async (newPassword) => {
        if (!passwordUser?.username) return
        setSavingPass(true)
        try {
            await api.put('/users/password', {
                username: passwordUser.username,
                newPassword,
            })

            message.success('Пароль успешно изменён')
            setPasswordUser(null)
        } catch (e) {
            console.error(e)
            message.error(e.message || 'Ошибка при смене пароля')
        } finally {
            setSavingPass(false)
        }
    }

    // =========================================================
    // 🔹 Закрытие модалок
    // =========================================================
    const onCancelEdit = () => setEditingUser(null)
    const onCancelPassword = () => setPasswordUser(null)

    // =========================================================
    // 🔹 Рендер
    // =========================================================
    return (
        <>
            <UsersTable
                users={users}
                loading={loading}
                onEdit={onEdit}
                onChangePassword={onChangePassword}
            />

            <UserEditModal
                open={!!editingUser}
                user={editingUser}
                saving={savingEdit}
                onCancel={onCancelEdit}
                onSave={onSave}
            />

            <ChangePasswordModal
                open={!!passwordUser}
                user={passwordUser}
                saving={savingPass}
                onCancel={onCancelPassword}
                onSave={onSavePassword}
            />
        </>
    )
}
