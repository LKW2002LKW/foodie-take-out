import { noticeTagTypeMap } from '@/constants/notice'

export const getNoticeTagType = (type) => noticeTagTypeMap[type] || 'default'

export const canReadUserNotice = (detail) => (
  (detail?.target == 2 || detail?.target == 3) && detail?.status == 1
)

export const getNoticeBrief = (content, maxLength = 40) => {
  if (!content) return ''

  const plainText = content.replace(/\n/g, ' ')
  return plainText.length > maxLength
    ? `${plainText.substring(0, maxLength)}...`
    : plainText
}

export const formatNoticeTime = (timeStr) => {
  if (!timeStr) return ''

  try {
    return timeStr.substring(5, 16)
  } catch (error) {
    return timeStr
  }
}

export const formatNoticeContentHtml = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}
