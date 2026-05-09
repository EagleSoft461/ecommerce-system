{{/*
Common labels
*/}}
{{- define "ecommerce.labels" -}}
app.kubernetes.io/managed-by: Helm
app.kubernetes.io/version: {{ .Chart.AppVersion }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{/*
Namespace
*/}}
{{- define "ecommerce.namespace" -}}
{{ .Values.namespace }}
{{- end }}
