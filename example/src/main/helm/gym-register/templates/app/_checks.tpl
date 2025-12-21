{{ define "gymregister.app.check" }}
    {{ if ne nil (.Values.gymRegisterApp).walkInPolicy }}
        {{ $knownWalkInPolicies := list "allow" "disallow" }}
        {{ if not (has (.Values.gymRegisterApp).walkInPolicy $knownWalkInPolicies) }}
            {{ fail (printf "Unrecognised walk-in policy '%s'. Set gymRegisterApp.walkInPolicy to one of %s and try again." (.Values.gymRegisterApp).walkInPolicy $knownWalkInPolicies) }}
        {{ end }}
    {{ end }}
{{ end }}
