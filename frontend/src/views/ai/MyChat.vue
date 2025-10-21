<template>
  <div class="chat-container">
    <!-- 左侧历史记录 -->
    <div class="history-sidebar">
      <div class="history-header">历史记录</div>
      <div class="history-list">
        <div
          v-for="(session, index) in historySessions"
          :key="index"
          class="history-session"
          @click="loadHistory(index)"
        >
          <div
            v-for="(msg, msgIndex) in session"
            :key="msgIndex"
            class="history-message"
            :class="msg.role"
          >
            {{ msg.content.slice(0, 30) }}...
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧当前对话 -->
    <div class="current-chat">
      <div class="messages" ref="messagesContainer">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <div v-if="msg.role === 'assistant'" class="assistant-content">
            <div v-if="getParsedContent(msg.content).thinking" class="thinking-section">
              {{ getParsedContent(msg.content).thinking }}
            </div>
            <div v-if="getParsedContent(msg.content).answer" class="answer-section">
              {{ getParsedContent(msg.content).answer }}
            </div>
          </div>
          <div v-else class="content user-content">
            {{ msg.content }}
          </div>
        </div>
      </div>

      <div class="input-area">
        <input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="输入消息..."
          class="input-box"
        />
        <button @click="sendMessage" class="send-btn">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyChat',
  data() {
    return {
      inputMessage: '',
      messages: [],
      historySessions: [], // 存储历史对话
      eventSource: null
    }
  },
  methods: {
    async sendMessage() {
      if (!this.inputMessage.trim()) return

      // 保存当前对话到历史记录
      if (this.messages.length > 0) {
        this.historySessions.push([...this.messages])
      }

      // 添加用户消息（直接显示为结果）
      this.messages = [{
        role: 'user',
        content: this.inputMessage,
        type: 'result'
      }]

      // 创建新的流式连接
      this.createEventSource(this.inputMessage)
      this.inputMessage = ''
    },

    createEventSource(question) {
      if (this.eventSource) this.eventSource.close()

      this.eventSource = new EventSource(
        `/api/ai/streamChat?question=${encodeURIComponent(question)}`
      )

      // 初始化AI消息（思考状态）
      const aiMessage = {
        role: 'assistant',
        content: '',
        type: 'thinking'
      }
      this.messages.push(aiMessage)

      this.eventSource.onmessage = (event) => {
        if (event.data === '[DONE]') {
          // 将最后一条AI消息标记为结果
          const lastMsg = this.messages[this.messages.length - 1]
          if (lastMsg.role === 'assistant') {
            lastMsg.type = 'result'
          }
          this.eventSource.close()
          return
        }

        // 拼接流式内容
        const content = event.data.replace(/^data: /, '').trim()
        aiMessage.content += content
        this.scrollToBottom()
      }

      this.eventSource.onerror = (error) => {
        // console.error('连接错误:', error)
        this.eventSource.close()
      }
    },

    // 加载历史记录
    loadHistory(index) {
      this.messages = [...this.historySessions[index]]
      this.scrollToBottom()
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        container.scrollTop = container.scrollHeight
      })
    },

    // 解析AI回复内容，分离思考部分和回答部分
    getParsedContent(content) {
      const thinkRegex = /<think>([\s\S]*?)<\/think>/g
      const matches = thinkRegex.exec(content)

      if (matches) {
        const thinking = matches[1].trim()
        const answer = content.replace(/<think>[\s\S]*?<\/think>/g, '').trim()
        return { thinking, answer }
      }

      return { thinking: null, answer: content }
    }
  },
  beforeDestroy() {
    if (this.eventSource) this.eventSource.close()
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  max-width: 1200px;
  margin: 20px auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  height: 90vh;
}

/* 左侧历史记录样式 */
.history-sidebar {
  width: 300px;
  background: #f5f5f5;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.history-header {
  padding: 15px;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-session {
  padding: 10px;
  margin-bottom: 10px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.history-session:hover {
  background: #f0f0f0;
}

.history-message {
  font-size: 12px;
  margin: 5px 0;
  padding: 2px 5px;
  border-radius: 4px;
}

/* 右侧当前对话样式 */
.current-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f9f9f9;
}

.message {
  margin: 10px 0;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}



/* 助手消息容器 */
.assistant-content {
  max-width: 70%;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  overflow: hidden;
}

/* 思考部分样式 */
.thinking-section {
  background-color: #f5f5f5;
  color: #666;
  padding: 12px;
  line-height: 1.5;
  font-size: 14px;
  border-bottom: 1px solid #e0e0e0;
}

/* 答案部分样式 */
.answer-section {
  background-color: #ffffff;
  color: #000;
  padding: 12px;
  line-height: 1.5;
  font-size: 14px;
}

/* 用户消息样式 */
.user-content {
  max-width: 70%;
  padding: 12px;
  border-radius: 18px;
  line-height: 1.5;
  font-size: 14px;
  background-color: #0084ff;
  color: white;
  border-bottom-right-radius: 4px;
}

/* 输入区域样式 */
.input-area {
  display: flex;
  padding: 20px;
  background-color: white;
  border-top: 1px solid #ddd;
}

.input-box {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  margin-right: 10px;
  outline: none;
}

.send-btn {
  padding: 12px 24px;
  background-color: #0084ff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.send-btn:hover {
  background-color: #0066cc;
}
</style>
