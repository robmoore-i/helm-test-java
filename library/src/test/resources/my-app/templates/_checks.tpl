{{ define "my-app.image.pullPolicy.check" }}
    {{ if eq (.Values.image).pullPolicy "VeryBad" }}
        {{ fail "Don't use the VeryBad image pull policy!" }}
    {{ end }}
{{ end }}