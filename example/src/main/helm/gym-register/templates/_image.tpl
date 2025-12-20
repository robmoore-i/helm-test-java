{{ define "gymregister.image.name" }}
    {{- printf "%s:%s" .name .Chart.AppVersion }}
{{ end }}
