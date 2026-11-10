
set -e

OFFERING_URL="http://localhost:8080/offerings/CS201-FALL-2026"

step() {
  echo
  echo "=== $1"
}

enroll() {
  curl -s -w "\nHTTP %{http_code}\n" -X POST "$OFFERING_URL/enrollments" \
    -H "Content-Type: application/json" \
    -d "{\"learnerId\":\"$1\"}"
}

drop() {
  curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$OFFERING_URL/enrollments/$1"
}

state() {
  curl -s -w "\nHTTP %{http_code}\n" "$OFFERING_URL"
}

step "Enroll L1 (expect 201 ENROLLED)"
enroll L1

step "Enroll L2 (expect 201 ENROLLED)"
enroll L2

step "Enroll L3 when full (expect 201 WAITLISTED, position 1)"
enroll L3

step "Enroll L4 without the prerequisite (expect 422 PREREQUISITE_NOT_MET)"
enroll L4

step "Enroll L1 again (expect 200 ALREADY_ENROLLED)"
enroll L1

step "State (expect enrolled L1, L2 and waitlist L3)"
state

step "Drop L1 (expect 200 DROPPED, promoted L3)"
drop L1

step "Drop L1 again (expect 409 NOT_ENROLLED)"
drop L1

step "State (expect enrolled L2, L3 and an empty waitlist)"
state
