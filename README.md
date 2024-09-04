]# Run the SQL query
echo "running SQL: ${SQL}"
MANIFEST_ID=$(sh dbaccess_readonly.sh -autosys <<EOF
select bpl.manifest_id from cash_owner.batch_post_log bpl left join cash_owner.csh_...
EOF
)

# Check for dbaccess exit status
if [ $? -ne 0 ]; then
  MSG="dbaccess failed to run SQL, please retry"
  echo "${MSG} MANIFEST_ID=${MANIFEST_ID}"
  exit 1
fi

if [ -z "$MANIFEST_ID" ]; then
  echo "No bad data found in batch_post_log, job can run."
else
  found_manifest_id=false
  items_after_manifest_id=()

  echo "Already active manifest IDs:"
  for item in $MANIFEST_ID; do
    if [ "$found_manifest_id" = false ]; then
      if [ "$item" == 'MANIFEST_ID' ]; then
        found_manifest_id=true
      fi
    else
      items_after_manifest_id+=("$item")
    fi
  done

  if [ ${#items_after_manifest_id[@]} -eq 0 ]; then
    echo "No bad data found in batch_post_log, job can run."
  else
    echo "Items after MANIFEST_ID:"
    for item in "${items_after_manifest_id[@]}"; do
      # Check if the item contains only letters, digits, and hyphens
      if [[ "$item" =~ ^[a-zA-Z0-9-]+$ ]]; then
        echo "Found manifestId '$item' in batch_post_log for payload_id in ('$item') that does not have cash data."
        echo "CASH L3, please run this on batch database for the job to be able to run:"
        echo "delete from cash_owner.merch_dtb_pst where manifest_id='$item';"
        echo "delete from cash_owner.batch_post_log where manifest_id='$item';"
        echo "this bad data must be cleaned up before the job can run successfully"
      else
        echo "Invalid manifest ID found: '$item'. Skipping."
      fi
    done
  fi

  # Exit the script with status 1 if any manifest_id is found
  exit 1
fi
