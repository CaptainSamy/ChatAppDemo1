package com.example.chatappdemo.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappdemo.R;
import com.example.chatappdemo.model.Contact;
import com.example.chatappdemo.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {
    private Context context;
    private ArrayList<Contact> contactList;
    private String groupId, myGroupRole;

    public AdapterParticipantAdd(Context context, ArrayList<Contact> contactList, String groupId, String myGroupRole) {
        this.context = context;
        this.contactList = contactList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_panticipant_add, parent, false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
            Contact modelContact = contactList.get(position);
            String name = modelContact.getName();
            String image = modelContact.getImgAnhDD();
            String uid = modelContact.getUid();

            holder.nameTv.setText(name);
            try {
                Picasso.get().load(image).placeholder(R.drawable.user_profile).into(holder.avatarIv);
            }catch (Exception e) {
                holder.avatarIv.setImageResource(R.drawable.user_profile);
            }
            checkIfAlreadyExists(modelContact, holder);
            //
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                    ref.child(groupId).child("Participants").child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // user exists/ participant
                                        String hisPreviosRole = ""+snapshot.child("role").getValue();
                                        // option
                                        String[] options;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Choose Option");
                                        if (myGroupRole.equals("Creator")){
                                            if (hisPreviosRole.equals("Admin")) {
                                                options = new String[]{"Remove Admin", "Remove User"};
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            //click remove admin
                                                            removeAdmin(modelContact);
                                                        }else {
                                                            // click remove user
                                                            removeParticipant(modelContact);
                                                        }
                                                    }
                                                }).show();
                                            } else if (hisPreviosRole.equals("Participants")) {
                                                options = new String[]{"Make Admin", "Remove User"};
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            //click remove admin
                                                            makeAdmin(modelContact);
                                                        }else {
                                                            // click remove user
                                                            removeParticipant(modelContact);
                                                        }
                                                    }
                                                }).show();
                                            }
                                        } else if (myGroupRole.equals("Admin")) {
                                            if (hisPreviosRole.equals("Creator")) {
                                                // im admin, he is creator
                                                Toast.makeText(context, "Creator of Group...",Toast.LENGTH_SHORT).show();
                                            } else if (hisPreviosRole.equals("Admin")) {
                                                // im admin, he is admin too
                                                options = new String[]{"Remove Admin", "Remove User"};
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            //click remove admin
                                                            removeAdmin(modelContact);
                                                        }else {
                                                            // click remove user
                                                            removeParticipant(modelContact);
                                                        }
                                                    }
                                                }).show();
                                            } else if (hisPreviosRole.equals("Participant")) {
                                                //im admin, he is participant
                                                options = new String[]{"Make Admin", "Remove User"};
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            //click remove admin
                                                            makeAdmin(modelContact);
                                                        }else {
                                                            // click remove user
                                                            removeParticipant(modelContact);
                                                        }
                                                    }
                                                }).show();
                                            }
                                        }
                                    } else {
                                        //user doesn't exists/ not participant: add
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Add Participant")
                                                .setMessage("Add this user in this group?")
                                                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        addParticipant(modelContact);
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            });
    }

    private void addParticipant(Contact modelContact) {
        //setup user data
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modelContact.getUid());
        hashMap.put("role", "Participant");
        hashMap.put("timestamp", ""+timestamp);
        // add that user in group>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelContact.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(Contact modelContact) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "Admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelContact.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "The user is now admin",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(Contact modelContact) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelContact.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "The user has been removed from the group",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(Contact modelContact) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "Participant");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelContact.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "The user is no longer admin",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(Contact modelContact, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        if (modelContact.getUid() != null) {
            ref.child(groupId).child("Participants").child(modelContact.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String hisRole = ""+snapshot.child("role").getValue();
                                holder.statusTv.setText(hisRole);
                            } else {
                                holder.statusTv.setText("");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        private CircleImageView avatarIv;
        private TextView nameTv, statusTv;
        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
