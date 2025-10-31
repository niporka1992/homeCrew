import React from 'react'
import { Alert } from 'antd'

export default function DailyMode() {
    return (
        <Alert
            message="Задача будет выполняться каждый день в указанное время."
            type="info"
            showIcon
            style={{ marginBottom: 12 }}
        />
    )
}
