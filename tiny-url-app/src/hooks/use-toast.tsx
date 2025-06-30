"use client"

import { toast as sonnerToast } from "sonner"

type ToastProps = {
  title: string
  description?: string
  variant?: "default" | "success" | "error" | "warning"
}

export function toast({ title, description, variant = "default" }: ToastProps) {
  return sonnerToast(title, {
    description,
    // You can map variant to different Sonner toast types if needed
    // For basic usage, this is sufficient
  })
}

// Re-export the Sonner toast functions for advanced usage if needed
export {
  toast as default,
  sonnerToast
}

