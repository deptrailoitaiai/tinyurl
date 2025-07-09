"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Copy, Link, QrCode, BarChart3, Settings, History, Zap } from "lucide-react"
import { toast } from "@/hooks/use-toast"

// TODO: Backend Integration - Replace with actual API calls
const mockUrls = [
  {
    id: "1",
    originalUrl: "https://www.example.com/very-long-url-that-needs-to-be-shortened",
    shortUrl: "tiny.ly/abc123",
    alias: "abc123",
    clicks: 1247,
    createdAt: "2024-01-15", 
    expiresAt: "2024-12-31",
    qrCode: "/placeholder.svg?height=100&width=100",
  },
  {
    id: "2",
    originalUrl: "https://github.com/vercel/next.js",
    shortUrl: "tiny.ly/nextjs",
    alias: "nextjs",
    clicks: 856,
    createdAt: "2024-01-10",
    expiresAt: null,
    qrCode: "/placeholder.svg?height=100&width=100",
  },
]

export default function TinyUrlApp() {
  const [url, setUrl] = useState("")
  const [customAlias, setCustomAlias] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [urls, setUrls] = useState(mockUrls)

  // TODO: Backend Integration - API call to shorten URL
  const handleShortenUrl = async () => {
    if (!url) return

    setIsLoading(true)

    // Simulate API call
    setTimeout(() => {
      const newUrl = {
        id: Date.now().toString(),
        originalUrl: url,
        shortUrl: `tiny.ly/${customAlias || Math.random().toString(36).substr(2, 6)}`,
        alias: customAlias || Math.random().toString(36).substr(2, 6),
        clicks: 0,
        createdAt: new Date().toISOString().split("T")[0],
        expiresAt: null,
        qrCode: "/placeholder.svg?height=100&width=100",
      }

      setUrls([newUrl, ...urls])
      setUrl("")
      setCustomAlias("")
      setIsLoading(false)

      toast({
        title: "URL shortened successfully!",
        description: "Your short URL is ready to use.",
      })
    }, 1000)

    /* 
    TODO: Backend API call example:
    try {
      const response = await fetch('/api/shorten', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          url, 
          customAlias,
          expiresAt: null // Add expiration logic
        })
      })
      const data = await response.json()
      // Handle response
    } catch (error) {
      // Handle error
    }
    */
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    toast({
      title: "Copied to clipboard!",
      description: "Short URL has been copied.",
    })
  }

  // TODO: Backend Integration - API call to get analytics
  const getAnalytics = (urlId: string) => {
    console.log("Get analytics for URL:", urlId)
    /* 
    TODO: Backend API call:
    fetch(`/api/analytics/${urlId}`)
      .then(res => res.json())
      .then(data => {
        // Show analytics modal/page
      })
    */
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-4">
            <Link className="h-8 w-8 text-blue-600" />
            <h1 className="text-4xl font-bold text-gray-900">TinyURL Pro</h1>
          </div>
          <p className="text-gray-600 text-lg">Shorten, customize, and track your URLs with advanced analytics</p>
        </div>

        <Tabs defaultValue="shorten" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="shorten" className="flex items-center gap-2">
              <Zap className="h-4 w-4" />
              Shorten
            </TabsTrigger>
            <TabsTrigger value="history" className="flex items-center gap-2">
              <History className="h-4 w-4" />
              History
            </TabsTrigger>
            <TabsTrigger value="analytics" className="flex items-center gap-2">
              <BarChart3 className="h-4 w-4" />
              Analytics
            </TabsTrigger>
            <TabsTrigger value="settings" className="flex items-center gap-2">
              <Settings className="h-4 w-4" />
              Settings
            </TabsTrigger>
          </TabsList>

          {/* Shorten URL Tab */}
          <TabsContent value="shorten">
            <Card>
              <CardHeader>
                <CardTitle>Shorten Your URL</CardTitle>
                <CardDescription>Enter a long URL to get a short, shareable link</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Original URL</label>
                  <Input
                    placeholder="https://example.com/very-long-url..."
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                    className="text-base"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium">Custom Alias (Optional)</label>
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">tiny.ly/</span>
                    <Input
                      placeholder="my-custom-link"
                      value={customAlias}
                      onChange={(e) => setCustomAlias(e.target.value)}
                      className="flex-1"
                    />
                  </div>
                </div>

                <Button onClick={handleShortenUrl} disabled={!url || isLoading} className="w-full" size="lg">
                  {isLoading ? "Shortening..." : "Shorten URL"}
                </Button>

                {/* Recent shortened URL */}
                {urls.length > 0 && (
                  <Card className="bg-green-50 border-green-200">
                    <CardContent className="pt-4">
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <p className="text-sm text-gray-600 mb-1">Your shortened URL:</p>
                          <p className="font-mono text-lg text-green-700">{urls[0].shortUrl}</p>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm" onClick={() => copyToClipboard(urls[0].shortUrl)}>
                            <Copy className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              // TODO: Backend Integration - Generate QR Code
                              console.log("Generate QR Code for:", urls[0].shortUrl)
                            }}
                          >
                            <QrCode className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* History Tab */}
          <TabsContent value="history">
            <Card>
              <CardHeader>
                <CardTitle>URL History</CardTitle>
                <CardDescription>Manage all your shortened URLs</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {urls.map((urlItem) => (
                    <Card key={urlItem.id} className="p-4">
                      <div className="flex items-center justify-between">
                        <div className="flex-1 space-y-2">
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-blue-600">{urlItem.shortUrl}</span>
                            <Badge variant="secondary">{urlItem.clicks} clicks</Badge>
                          </div>
                          <p className="text-sm text-gray-600 truncate max-w-md">{urlItem.originalUrl}</p>
                          <p className="text-xs text-gray-500">
                            Created: {urlItem.createdAt}
                            {urlItem.expiresAt && ` • Expires: ${urlItem.expiresAt}`}
                          </p>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm" onClick={() => copyToClipboard(urlItem.shortUrl)}>
                            <Copy className="h-4 w-4" />
                          </Button>
                          <Button variant="outline" size="sm" onClick={() => getAnalytics(urlItem.id)}>
                            <BarChart3 className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              // TODO: Backend Integration - Generate QR Code
                              console.log("Generate QR Code for:", urlItem.shortUrl)
                            }}
                          >
                            <QrCode className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Analytics Tab */}
          <TabsContent value="analytics">
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              <Card>
                <CardHeader>
                  <CardTitle>Total Clicks</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-blue-600">
                    {urls.reduce((sum, url) => sum + url.clicks, 0)}
                  </div>
                  <p className="text-sm text-gray-600">Across all URLs</p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Total URLs</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-green-600">{urls.length}</div>
                  <p className="text-sm text-gray-600">URLs created</p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Top Performer</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-lg font-semibold text-purple-600">
                    {urls.sort((a, b) => b.clicks - a.clicks)[0]?.alias || "N/A"}
                  </div>
                  <p className="text-sm text-gray-600">
                    {urls.sort((a, b) => b.clicks - a.clicks)[0]?.clicks || 0} clicks
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* TODO: Backend Integration - Detailed Analytics Charts */}
            <Card className="mt-6">
              <CardHeader>
                <CardTitle>Click Analytics</CardTitle>
                <CardDescription>Detailed analytics will be displayed here</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
                  <p className="text-gray-500">Analytics charts will be implemented with backend data</p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Settings Tab */}
          <TabsContent value="settings">
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>Account Settings</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Default Domain</label>
                    <Input defaultValue="tiny.ly" />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">API Key</label>
                    <div className="flex gap-2">
                      <Input defaultValue="sk-..." type="password" />
                      <Button variant="outline">Generate</Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Preferences</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <label className="text-sm font-medium">Auto-generate QR codes</label>
                    <input type="checkbox" className="rounded" />
                  </div>
                  <div className="flex items-center justify-between">
                    <label className="text-sm font-medium">Email notifications</label>
                    <input type="checkbox" className="rounded" defaultChecked />
                  </div>
                  <div className="flex items-center justify-between">
                    <label className="text-sm font-medium">Public analytics</label>
                    <input type="checkbox" className="rounded" />
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}
